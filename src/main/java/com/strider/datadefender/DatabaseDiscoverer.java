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
import static java.util.regex.Pattern.compile;
import static org.apache.log4j.Logger.getLogger;
import org.apache.commons.collections.ListUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import opennlp.tools.util.Span;

import org.apache.log4j.Logger;

import com.strider.datadefender.database.IDBFactory;
import com.strider.datadefender.database.metadata.IMetaData;
import com.strider.datadefender.database.metadata.MatchMetaData;
import com.strider.datadefender.database.sqlbuilder.ISQLBuilder;
import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.utils.SQLToJavaMapping;
import java.util.Arrays;
import java.util.Locale;


/**
 *
 * @author Armenak Grigoryan
 */
public class DatabaseDiscoverer extends Discoverer {
    
    private static Logger log = getLogger(DatabaseDiscoverer.class);
    
    private static String[] modelList;
        
    public List<MatchMetaData> discover(final IDBFactory factory, final Properties dataDiscoveryProperties, final Set<String> tables) 
    throws AnonymizerException {
        log.info("Data discovery in process");

        // Get the probability threshold from property file
        double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        log.info("Probability threshold [" + probabilityThreshold + "]");
        
        // Get list of models used in data discovery
        final String models = dataDiscoveryProperties.getProperty("models");
        modelList = models.split(",");
        log.info("Model list [" + Arrays.toString(modelList) + "]");
        
        List<MatchMetaData> finalList = new ArrayList<>();
        for (String model: modelList) {
            log.info("********************************");
            log.info("Processing model " + model);
            log.info("********************************");
            Model modelPerson = createModel(dataDiscoveryProperties, model);
            matches = discoverAgainstSingleModel(factory, dataDiscoveryProperties, tables, modelPerson, probabilityThreshold);
            finalList = ListUtils.union(finalList, matches);            
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");                    
        log.info("List of suspects:");
        log.info(String.format("%20s %20s %20s", "Table*", "Column*", "Probability*"));        
        for(MatchMetaData data: finalList) {    
            String probability = decimalFormat.format(data.getAverageProbability());
            String result = String.format("%20s %20s %20s %20s", data.getTableName(), data.getColumnName(), probability, data.getModel());
            log.info(result);            
        }                    
        
        return matches;
    }
    
    private List<MatchMetaData> discoverAgainstSingleModel(final IDBFactory factory, final Properties dataDiscoveryProperties, 
            final Set<String> tables, final Model model, final double probabilityThreshold)
    throws AnonymizerException {
        IMetaData metaData = factory.fetchMetaData();
        List<MatchMetaData> map = metaData.getMetaData();
        // Start running NLP algorithms for each column and collect percentage
        matches = new ArrayList<>();

        ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        List<Double> probabilityList;
        for(MatchMetaData data: map) {
            if (SQLToJavaMapping.isString(data.getColumnType())) {
                String tableName = data.getTableName();
                String columnName = data.getColumnName();
                probabilityList = new ArrayList<>();
                
                log.info("Analyzing table [" + tableName + "]");
                
                if (!tables.isEmpty() && !tables.contains(tableName.toLowerCase(Locale.ENGLISH))) {
                    log.debug("Continue ...");
                    continue;
                }
                
                String tableNamePattern = dataDiscoveryProperties.getProperty("table_name_pattern");
                if (!CommonUtils.isEmptyString(tableNamePattern)) {
                    Pattern p = compile(tableNamePattern);
                    if (!p.matcher(tableName).matches()) {
                        continue;
                    }
                }
                
                final String table = sqlBuilder.prefixSchema(tableName);
                final int limit = Integer.parseInt(dataDiscoveryProperties.getProperty("limit"));
                final String query = sqlBuilder.buildSelectWithLimit(
                    "SELECT " + columnName + 
                    " FROM " + table + 
                    " WHERE " + columnName  + " IS NOT NULL ", limit);
                log.debug("Executing query against database: " + query);
                
                try (Statement stmt = factory.getConnection().createStatement();
                    ResultSet resultSet = stmt.executeQuery(query);) {

                    while (resultSet.next()) {
                        final String sentence = resultSet.getString(1);
                        if (sentence != null && !sentence.isEmpty()) {
                            
                            // Convert sentence into tokens
                            final String tokens[] = model.getTokenizer().tokenize(sentence);
                            // Find names
                            final Span nameSpans[] = model.getNameFinder().find(tokens);
                            //find probabilities for names
                            final double[] spanProbs = model.getNameFinder().probs(nameSpans);
                            //display names
                            for( int i = 0; i<nameSpans.length; i++) {
                                //log.debug("Span: "+nameSpans[i].toString());
		    		//log.debug("Covered text is: "+tokens[nameSpans[i].getStart()]);
		    		//log.debug("Probability is: "+spanProbs[i]);
                                probabilityList.add(spanProbs[i]);
                            }
                            // From OpenNLP documentation
                            //  After every document clearAdaptiveData must be called to clear the adaptive data in the feature generators. 
                            // Not calling clearAdaptiveData can lead to a sharp drop in the detection rate after a few documents. 
                            model.getNameFinder().clearAdaptiveData();    
                        }
                    }
                } catch (SQLException sqle) {
                    log.error(sqle.toString());
                }
                
                double averageProbability = calculateAverage(probabilityList);
                if ((averageProbability >= probabilityThreshold)) {
                    data.setAverageProbability(averageProbability);
                    data.setModel(model.getName());
                    matches.add(data);
                }
            }
        }
        return matches;
    }
}