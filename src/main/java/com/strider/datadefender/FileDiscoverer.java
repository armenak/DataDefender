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

import static java.lang.Double.parseDouble;
import static org.apache.log4j.Logger.getLogger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import opennlp.tools.util.Span;

import com.strider.datadefender.file.metadata.FileMatchMetaData;

/**
 *
 * @author Armenak Grigoryan
 */
public class FileDiscoverer extends Discoverer {
    
    private static final Logger log = getLogger(FileDiscoverer.class);
    
    private static String[] modelList;
    protected List<FileMatchMetaData> fileMatches;
        
    public List<FileMatchMetaData> discover(final Properties dataDiscoveryProperties) 
    throws AnonymizerException, IOException, SAXException, TikaException {
        log.info("Data discovery in process");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        log.info("Probability threshold [" + probabilityThreshold + "]");
        
        // Get list of models used in data discovery
        final String models = dataDiscoveryProperties.getProperty("models");
        modelList = models.split(",");
        log.info("Model list [" + Arrays.toString(modelList) + "]");
        
        List<FileMatchMetaData> finalList = new ArrayList<>();
        for (String model: modelList) {
            log.info("********************************");
            log.info("Processing model " + model);
            log.info("********************************");
            final Model modelPerson = createModel(dataDiscoveryProperties, model);
            fileMatches = discoverAgainstSingleModel(dataDiscoveryProperties, modelPerson, probabilityThreshold);
            finalList = ListUtils.union(finalList, fileMatches);            
        }

        final DecimalFormat decimalFormat = new DecimalFormat("#.##");                    
        log.info("List of suspects:");
        log.info(String.format("%20s %20s %20s", "Table*", "Column*", "Probability*"));        
        for(final FileMatchMetaData data: finalList) {    
            final String probability = decimalFormat.format(data.getAverageProbability());
            final String result = String.format("%20s %20s %20s %20s", data.getDirectory(), data.getFileName(), probability, data.getModel());
            log.info(result);            
        }                    
        
        return fileMatches;
    }
    
    private List<FileMatchMetaData> discoverAgainstSingleModel(final Properties fileDiscoveryProperties, final Model model, final double probabilityThreshold)
    throws AnonymizerException, IOException, SAXException, TikaException {
        // Start running NLP algorithms for each column and collect percentage
        fileMatches = new ArrayList<>();
        String[] directoryList = null;
        final String directories = fileDiscoveryProperties.getProperty("directories");
        directoryList = directories.split(",");
        
        // Let's iterate over directories
        File node;
        Metadata metadata;
        List<Double> probabilityList;
        for (final String directory: directoryList) {
            
            node = new File(directory);
            
            if (node.isDirectory()) {
                final String[] files = node.list();
                for (final String file: files) {
                    // Detect file type
                    log.info("Analyzing " + directory + "/" + file);
                    
                    final BodyContentHandler handler = new BodyContentHandler();
                    
                    final AutoDetectParser parser = new AutoDetectParser();
                    metadata = new Metadata();
                    String handlerString = "";
                    try (InputStream stream = new FileInputStream(directory + "/" + file)) {
                        if (stream != null) {
                            parser.parse(stream, handler, metadata);
                            handlerString =  handler.toString();
                        }
                    }                    
                    
                    log.info("Content: " + handlerString);
                    final String tokens[] = model.getTokenizer().tokenize(handler.toString());
                    final Span nameSpans[] = model.getNameFinder().find(tokens);
                    final double[] spanProbs = model.getNameFinder().probs(nameSpans);
                    //display names
                    probabilityList = new ArrayList<>();
                    for( int i = 0; i < nameSpans.length; i++) {
                        log.info("Span: "+nameSpans[i].toString());
                        log.info("Covered text is: "+tokens[nameSpans[i].getStart()]);
                        log.info("Probability is: "+spanProbs[i]);
                        probabilityList.add(spanProbs[i]);
                    }      
                    model.getNameFinder().clearAdaptiveData(); 
                    
                    final double averageProbability = calculateAverage(probabilityList);
                    if ((averageProbability >= probabilityThreshold)) {
                        final FileMatchMetaData result = new FileMatchMetaData(directory, file);
                        result.setAverageProbability(averageProbability);
                        result.setModel(model.getName());
                        fileMatches.add(result);
                    }                    
                }
            }
        }
        
        return fileMatches;
    }
}