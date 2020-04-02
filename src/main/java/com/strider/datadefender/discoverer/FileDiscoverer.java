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
 */
package com.strider.datadefender.discoverer;

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.ModelDiscoveryConfig;
import com.strider.datadefender.file.metadata.FileMatchMetaData;
import com.strider.datadefender.functions.Utils;
import com.strider.datadefender.specialcase.SpecialCase;

import java.text.DecimalFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.sql.SQLException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.SAXException;

import opennlp.tools.util.Span;

import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Armenak Grigoryan
 */
@Log4j2
public class FileDiscoverer extends Discoverer {

    protected final List<File> directories;
    protected List<FileMatchMetaData> fileMatches;
    protected List<String> excludeExtensions;

    public FileDiscoverer(ModelDiscoveryConfig config, List<File> directories, List<String> excludeExtensions) throws IOException {
        super(config);
        this.directories = directories;
        this.excludeExtensions = excludeExtensions;
    }

    public List<FileMatchMetaData> discover()
        throws FileDiscoveryException,
        DataDefenderException,
        IOException,
        SAXException,
        TikaException {

        List<FileMatchMetaData> finalList = new ArrayList<>();

        for (final String sm : CollectionUtils.emptyIfNull(config.getModels())) {
            log.info("********************************");
            log.info("Processing model " + sm);
            log.info("********************************");

            final Model model = createModel(sm);
            fileMatches = discoverAgainstSingleModel(model);
            finalList = ListUtils.union(finalList, fileMatches);
        }
        for (final File fm : CollectionUtils.emptyIfNull(config.getFileModels())) {
            log.info("********************************");
            log.info("Processing model " + fm);
            log.info("********************************");

            final Model model = createModel(fm);
            fileMatches = discoverAgainstSingleModel(model);
            finalList = ListUtils.union(finalList, fileMatches);
        }

        // Special case
        List<String> specialCaseFunctions = config.getExtensions();
        boolean specialCase = CollectionUtils.isNotEmpty(specialCaseFunctions);
        
        if (specialCase) {
            Metadata          metadata;
            try {
                log.info("**************" + specialCaseFunctions.toString());
                for (String fn : CollectionUtils.emptyIfNull(specialCaseFunctions)) {
                    for (final File node : directories) {
                        final List<File> files = (List<File>) FileUtils.listFiles(node, null, true);

                        for (final File fich : files) {
                            final String file         = fich.getName();
                            final String recursivedir = fich.getParent();

                            log.info("Analyzing [" + fich.getCanonicalPath() + "]");
                            final String ext = FilenameUtils.getExtension(fich.getName()).toLowerCase(Locale.ENGLISH);
                            log.debug("Extension: " + ext);

                            if (CollectionUtils.containsAny(excludeExtensions, ext)) {
                                log.info("Ignoring type " + ext);
                                continue;
                            }

                            final BodyContentHandler handler = new BodyContentHandler(-1);
                            final AutoDetectParser   parser  = new AutoDetectParser();

                            metadata = new Metadata();

                            String handlerString = "";
                            try (final InputStream stream = new FileInputStream(fich.getCanonicalPath())) {
                                
                                log.debug("Loading data into the stream");
                                if (stream != null) {
                                    parser.parse(stream, handler, metadata);
                                    handlerString = handler.toString().replaceAll("( )+", " ").replaceAll("[\\t\\n\\r]+"," ");

                                    String[] tokens = handlerString.split(" ");

                                    for (int t=0; t<tokens.length; t++) {
                                        String token = tokens[t];
                                        if (token.trim().length() < 1) {
                                            continue;
                                        }
                                        log.info(fn);
                                        FileMatchMetaData returnData = null;
                                        try {
                                            returnData = 
                                                (FileMatchMetaData)callExtension(new FileMatchMetaData(recursivedir, file), fn, token);
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
                    }
                }
            } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | 
                    SecurityException | SQLException | TikaException | SAXException e) {
                log.error(e.toString());
            }
        }

        
        final DecimalFormat decimalFormat = new DecimalFormat("#.##");

        log.info("List of suspects:");
        log.info(String.format("%40s %20s %20s %20s", "Directory*", "File*", "Probability*", "Model*"));
        
        finalList = uniqueList(finalList);
        
        Collections.sort(finalList, Comparator.comparing(FileMatchMetaData ::getFileName));
        
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

    private List<FileMatchMetaData> discoverAgainstSingleModel(final Model model)
        throws DataDefenderException,
        IOException,
        SAXException,
        TikaException {

        // Start running NLP algorithms for each column and collect percentage
        fileMatches = new ArrayList<>();

        log.info("Directories to analyze: {}", StringUtils.join(directories, ","));

        // Let's iterate over directories
        Metadata          metadata;
        for (final File node : directories) {

            final List<File> files = (List<File>) FileUtils.listFiles(node, null, true);

            for (final File fich : files) {
                final String file         = fich.getName();
                final String recursivedir = fich.getParent();

                log.info("Analyzing [" + fich.getCanonicalPath() + "]");
                final String ext = FilenameUtils.getExtension(fich.getName()).toLowerCase(Locale.ENGLISH);
                log.debug("Extension: " + ext);

                if (CollectionUtils.containsAny(excludeExtensions, ext)) {
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

                fileMatches = getMatchedFiles(model, handler.toString(), file, recursivedir);
            }
        }

        return fileMatches;
    }
    
    
    protected List<FileMatchMetaData> getMatchedFiles(final Model model, String handler, String file, String recursivedir) {

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

        if (averageProbability >= config.getProbabilityThreshold()) {
            final FileMatchMetaData result = new FileMatchMetaData(recursivedir, file);

            result.setAverageProbability(averageProbability);
            result.setModel(model.getName());
            fileMatches.add(result);
        }
        
        return fileMatches;
    }

    private Object callExtension(final FileMatchMetaData metadata, final String function, final String text)
            throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException 
    {
        if (StringUtils.isBlank(function)) {
            return null;
        }

        Object value = null;

        try {
            final String className = Utils.getClassName(function);
            final String methodName = Utils.getMethodName(function);
            final Method method = Class.forName(className).getMethod(
                methodName,
                new Class[] { FileMatchMetaData.class, String.class }
            );
            final SpecialCase instance = (SpecialCase) Class.forName(className).getConstructor().newInstance();
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