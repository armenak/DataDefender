/*
 *
 * Copyright 2014-2015, Armenak Grigoryan, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package com.strider.datadefender;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static java.lang.Double.parseDouble;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.SAXException;

import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.file.metadata.FileMatchMetaData;
import com.strider.datadefender.functions.Utils;
import com.strider.datadefender.specialcase.SpecialCase;
import com.strider.datadefender.utils.CommonUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import opennlp.tools.util.Span;

/**
 *
 * @author Armenak Grigoryan
 */
public class FileDiscoverer extends Discoverer {
    private static final Logger       log = getLogger(FileDiscoverer.class);
    private static String[]           modelList;
    protected List<FileMatchMetaData> fileMatches;

    @SuppressWarnings("unchecked")
    public List<FileMatchMetaData> discover(final Properties fileDiscoveryProperties)
            throws FileDiscoveryException, DatabaseDiscoveryException, IOException, SAXException, TikaException {
        log.info("Data discovery in process");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(fileDiscoveryProperties.getProperty("probability_threshold"));

        log.info("Probability threshold [" + probabilityThreshold + "]");

        // Get list of models used in data discovery
        final String models = fileDiscoveryProperties.getProperty("models");

        modelList = models.split(",");
        log.info("Model list [" + Arrays.toString(modelList) + "]");

        List<FileMatchMetaData> finalList = new ArrayList<>();

        for (final String model : modelList) {
            log.info("********************************");
            log.info("Processing model " + model);
            log.info("********************************");

            final Model modelPerson = createModel(fileDiscoveryProperties, model);

            fileMatches = discoverAgainstSingleModel(fileDiscoveryProperties, modelPerson, probabilityThreshold);
            finalList   = ListUtils.union(finalList, fileMatches);
        }

        // Special case
        String[] specialCaseFunctions = null;
        boolean specialCase = false;
        final String extentionList = fileDiscoveryProperties.getProperty("extentions");
        
        final String directories   = fileDiscoveryProperties.getProperty("directories");
        final String   exclusions    = fileDiscoveryProperties.getProperty("exclusions");
        String exclusionList[] = null;
        if ((exclusions == null) || exclusions.equals("")) {
            log.info("exclusions property is empty in firediscovery.properties file");
        } else {
            exclusionList = exclusions.split(",");
            log.info("File types not considered for analysis: " + exclusions);
        }
        log.info("Directories to analyze: " + directories);

        if ((directories == null) || directories.equals("")) {
            log.error("directories property is empty in firediscovery.properties file");

            throw new DatabaseDiscoveryException("directories property is empty in firediscovery.properties file");
        }

        final String[] directoryList = directories.split(",");        
        if (!CommonUtils.isEmptyString(extentionList)) {
            log.info("***** Extension list:" + extentionList);
            specialCaseFunctions = extentionList.split(",");

            if ((specialCaseFunctions != null) && (specialCaseFunctions.length > 0)) {
                log.debug("special case:" + specialCase);
                File              node;
                Metadata          metadata;
                
                try {
                    log.info("**************" + specialCaseFunctions.toString());
                    for (int j = 0; j < specialCaseFunctions.length; j++) {
                        for (final String directory : directoryList) {
                            
                            node = new File(directory);
                            final List<File> files = (List<File>) FileUtils.listFiles(node, null, true);
                            
                            for (final File fich : files) {
                                final String file         = fich.getName();
                                final String recursivedir = fich.getParent();

                                log.info("Analyzing [" + fich.getCanonicalPath() + "]");
                                final String ext = CommonUtils.getFileExtension(fich).toLowerCase(Locale.ENGLISH);
                                log.debug("Extension: " + ext);

                                if ((exclusionList != null) && Arrays.asList(exclusionList).contains(ext)) {
                                    log.info("Ignoring type " + ext);
                                    continue;
                                }

                                final BodyContentHandler handler = new BodyContentHandler(-1);
                                final AutoDetectParser   parser  = new AutoDetectParser();

                                metadata = new Metadata();

                                String handlerString = "";
                                try {
                                    final InputStream stream = new FileInputStream(fich.getCanonicalPath());
                                    log.debug("Loading data into the stream");
                                    if (stream != null) {
                                        parser.parse(stream, handler, metadata);
                                        handlerString = handler.toString();
                                        
                                        handlerString = handlerString.replaceAll("( )+", " ");
                                        
                                        handlerString = handlerString.replaceAll("[\\t\\n\\r]+"," ");
                                        
                                        String[] tokens = handlerString.split(" ");
                                        
                                        for (int t=0; t<tokens.length; t++) {
                                            String token = tokens[t];
                                            if (token.trim().length() < 1) {
                                                continue;
                                            }
                                            String specialFunction  = specialCaseFunctions[j];
                                            log.info(specialFunction);
                                            FileMatchMetaData returnData = null;
                                            try {
                                                returnData = 
                                                    (FileMatchMetaData)callExtention(new FileMatchMetaData(recursivedir, file), specialFunction,token);
                                            } catch (InvocationTargetException e) {
                                                continue;
                                            }
                                            if (returnData != null) {
                                                returnData.setModel("sin");
                                                returnData.setAverageProbability(1.0);
                                                List<FileMatchMetaData> specialFileMatches = new ArrayList();
                                                specialFileMatches.add(returnData);
                                                
                                                finalList   = ListUtils.union(finalList, specialFileMatches);
                                            }
                                            log.debug(tokens[t]);                                            
                                        }
                                        

                                    }
                                } catch (IOException e) {
                                    log.info("Unable to read " + fich.getCanonicalPath() + ".Ignoring...");
                                }
                                log.info("Finish processing " + fich.getCanonicalPath());
                            }
                            log.info("Finish speclai case " + specialCaseFunctions[j]);
                        }
                    }
                } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | 
                        SecurityException | SQLException | TikaException | SAXException e) {
                    log.error(e.toString());
                }
            }
        }

        
        final DecimalFormat decimalFormat = new DecimalFormat("#.##");

        log.info("List of suspects:");
        log.info(String.format("%40s %20s %20s %20s", "Directory*", "File*", "Probability*", "Model*"));
        
        finalList = uniqueList(finalList);
        for (final FileMatchMetaData data : finalList) {
            String result = "";
            final String probability = decimalFormat.format(data.getAverageProbability());
            result      = String.format("%40s %20s %20s %20s",
                                                     data.getDirectory(),
                                                     data.getFileName(),
                                                     probability,
                                                     data.getModel());
            log.info(result);
        }

        
        
        return Collections.unmodifiableList(fileMatches);
    }

    private List<FileMatchMetaData> discoverAgainstSingleModel(final Properties fileDiscoveryProperties,
                                                               final Model model, final double probabilityThreshold)
            throws DatabaseDiscoveryException, IOException, SAXException, TikaException {

        // Start running NLP algorithms for each column and collect percentage
        fileMatches = new ArrayList<>();

        final String directories   = fileDiscoveryProperties.getProperty("directories");

        log.info("Directories to analyze: " + directories);

        if ((directories == null) || directories.equals("")) {
            log.error("directories property is empty in firediscovery.properties file");

            throw new DatabaseDiscoveryException("directories property is empty in firediscovery.properties file");
        }

        final String[] directoryList = directories.split(",");
        String[]       exclusionList = null;
        final String   exclusions    = fileDiscoveryProperties.getProperty("exclusions");

        if ((exclusions == null) || exclusions.equals("")) {
            log.info("exclusions property is empty in firediscovery.properties file");
        } else {
            exclusionList = exclusions.split(",");
            log.info("File types not considered for analysis: " + exclusions);
        }

        // Let's iterate over directories
        File              node;
        Metadata          metadata;

        for (final String directory : directoryList) {
            node = new File(directory);

            final List<File> files = (List<File>) FileUtils.listFiles(node, null, true);

            for (final File fich : files) {
                final String file         = fich.getName();
                final String recursivedir = fich.getParent();

                log.info("Analyzing [" + fich.getCanonicalPath() + "]");
                final String ext = CommonUtils.getFileExtension(fich).toLowerCase(Locale.ENGLISH);
                log.debug("Extension: " + ext);

                if ((exclusionList != null) && Arrays.asList(exclusionList).contains(ext)) {
                    log.info("Ignoring type " + ext);
                    continue;
                }

                final BodyContentHandler handler = new BodyContentHandler(-1);
                final AutoDetectParser   parser  = new AutoDetectParser();

                metadata = new Metadata();

                String handlerString = "";
                try {
                    final InputStream stream = new FileInputStream(fich.getCanonicalPath());
                    log.debug("Loading data into the stream");
                    if (stream != null) {
                        parser.parse(stream, handler, metadata);
                        handlerString = handler.toString();
                        log.debug(handlerString);
                    }
                } catch (IOException e) {
                    log.info("Unable to read " + fich.getCanonicalPath() + ".Ignoring...");
                }

                fileMatches = getMatchedFiles(model, probabilityThreshold, 
                        handler.toString(), file, recursivedir);
                
            }
        }

        return fileMatches;
    }
    
    
    protected List<FileMatchMetaData> getMatchedFiles(final Model model, 
            final double probabilityThreshold, String handler, 
            String file, String recursivedir) {
        
        final String   tokens[]    = model.getTokenizer().tokenize(handler);
        final Span     nameSpans[] = model.getNameFinder().find(tokens);
        final double[] spanProbs   = model.getNameFinder().probs(nameSpans);
        List<Probability> probabilityList = new ArrayList<>();

        for (int i = 0; i < nameSpans.length; i++) {
            log.info("Span: " + nameSpans[i].toString());
            log.info("Covered text is: " + tokens[nameSpans[i].getStart()]);
            log.info("Probability is: " + spanProbs[i]);
            probabilityList.add(new Probability(tokens[nameSpans[i].getStart()], spanProbs[i]));
        }

        model.getNameFinder().clearAdaptiveData();

        final double averageProbability = calculateAverage(probabilityList);

        if (averageProbability >= probabilityThreshold) {
            final FileMatchMetaData result = new FileMatchMetaData(recursivedir, file);

            result.setAverageProbability(averageProbability);
            result.setModel(model.getName());
            fileMatches.add(result);
        }
        
        return fileMatches;
    }

    private Object callExtention(final FileMatchMetaData metadata, final String function, final String text)
            throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException 
    {
        if ((function == null) || function.equals("")) {
            log.warn("Function " + function + " is not defined");
            return null;
        }

        Object value = null;

        try {
            final String className  = Utils.getClassName(function);
            final String methodName = Utils.getMethodName(function);
            final Method method     = Class.forName(className)
                                           .getMethod(methodName, new Class[] { FileMatchMetaData.class, String.class });
            final SpecialCase         instance    = (SpecialCase) Class.forName(className).newInstance();
            final Map<String, Object> paramValues = new HashMap<>(2);

            paramValues.put("metadata", metadata);
            paramValues.put("text", text);
            log.info("before");
            log.info(text);
            value = method.invoke(instance, metadata, text);
            log.info("after");
        } catch (InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }

        return value;
    }
    
    private static List<FileMatchMetaData> uniqueList(List<FileMatchMetaData> finalList) {
        
        HashSet hs = new HashSet();
        hs.addAll(finalList);
        finalList.clear();
        finalList.addAll(hs);
        
        return finalList;
    }

}