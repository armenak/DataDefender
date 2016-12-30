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
import com.strider.datadefender.extensions.BiographicFunctions;
import com.strider.datadefender.functions.CoreFunctions;
import com.strider.datadefender.functions.Utils;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.Parameter;
import com.strider.datadefender.specialcase.SinDetector;
import com.strider.datadefender.specialcase.SpecialCase;
import com.strider.datadefender.utils.CommonUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.ClassUtils;


/**
 *
 * @author Armenak Grigoryan
 */
public class DatabaseDiscoverer extends Discoverer {
    
    private static final Logger log = getLogger(DatabaseDiscoverer.class);
    
    private static String[] modelList;
        
    public List<MatchMetaData> discover(final IDBFactory factory, final Properties dataDiscoveryProperties, final Set<String> tables) 
    throws AnonymizerException, ParseException {
        log.info("Data discovery in process");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        log.info("Probability threshold [" + probabilityThreshold + "]");
        
        // Get list of models used in data discovery
        final String models = dataDiscoveryProperties.getProperty("models");
        modelList = models.split(",");
        log.info("Model list [" + Arrays.toString(modelList) + "]");
        
        List<MatchMetaData> finalList = new ArrayList<>();
        for (final String model: modelList) {
            log.info("********************************");
            log.info("Processing model " + model);
            log.info("********************************");
            final Model modelPerson = createModel(dataDiscoveryProperties, model);
            matches = discoverAgainstSingleModel(factory, dataDiscoveryProperties, tables, modelPerson, probabilityThreshold);
            finalList = ListUtils.union(finalList, matches);            
        }

        final DecimalFormat decimalFormat = new DecimalFormat("#.##");                    
        log.info("List of suspects:");
        log.info(String.format("%20s %20s %20s %20s", "Table*", "Column*", "Probability*", "Model*"));        
        for(final MatchMetaData data: finalList) {    
            final String probability = decimalFormat.format(data.getAverageProbability());
            final String result = String.format("%20s %20s %20s %20s", data.getTableName(), data.getColumnName(), probability, data.getModel());
            log.info(result);            
        }                    
        
        return matches;
    }
    
    private List<MatchMetaData> discoverAgainstSingleModel(final IDBFactory factory, final Properties dataDiscoveryProperties, 
            final Set<String> tables, final Model model, final double probabilityThreshold)
    throws AnonymizerException, ParseException {
        final IMetaData metaData = factory.fetchMetaData();
        final List<MatchMetaData> map = metaData.getMetaData();
        // Start running NLP algorithms for each column and collect percentage
        matches = new ArrayList<>();
        MatchMetaData specialCaseData = null;
        boolean specialCase = false;
        
        
        final String[] specialCaseFunctions = dataDiscoveryProperties.getProperty("extentions").split(",");
        if (specialCaseFunctions != null && specialCaseFunctions.length > 0) {
            specialCase = true;
        }
        
        final ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        List<Double> probabilityList;
        for(final MatchMetaData data: map) {
            final String tableName = data.getTableName();
            final String columnName = data.getColumnName();
            log.debug(data.getColumnType());
            probabilityList = new ArrayList<>();

            log.info("Analyzing table [" + tableName + "]");

            if (!tables.isEmpty() && !tables.contains(tableName.toLowerCase(Locale.ENGLISH))) {
                log.debug("Continue ...");
                continue;
            }

            final String tableNamePattern = dataDiscoveryProperties.getProperty("table_name_pattern");
            if (!CommonUtils.isEmptyString(tableNamePattern)) {
                final Pattern p = compile(tableNamePattern);
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
                    if (data.getColumnType().equals("BLOB") || data.getColumnType().equals("GEOMETRY")) {
                        continue;
                    }
                    
                    if (model.getName().equals("location") &&
                        data.getColumnType().contains("INT")) {
                        continue;
                    }
                    
                    final String sentence = resultSet.getString(1);
                    if (specialCase) {
                        try {
                            for (int i=0; i<specialCaseFunctions.length; i++) {
                                if (sentence != null && !sentence.equals("")) {
                                    specialCaseData = (MatchMetaData)callExtention(specialCaseFunctions[i], data, sentence);
                                }
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException  e) {
                            log.error(e.toString());
                        }
                        
                    }
                    
                    if (sentence != null && !sentence.isEmpty()) {
                        String processingValue = "";
                        if (data.getColumnType().equals("DATE") || 
                            data.getColumnType().equals("TIMESTAMP") ||
                            data.getColumnType().equals("DATETIME")
                           ) {
                            final DateFormat originalFormat = new SimpleDateFormat(sentence, Locale.ENGLISH);
                            final DateFormat targetFormat = new SimpleDateFormat("MMM d, yy");
                            final java.util.Date date = originalFormat.parse(sentence);
                            processingValue = targetFormat.format(date);
                        } else {
                            processingValue = sentence;
                        }
                        
                        //log.debug(sentence);
                        // Convert sentence into tokens
                        final String tokens[] = model.getTokenizer().tokenize(processingValue);
                        //for (int i=0; i<tokens.length;i++) {
                        //    log.debug("tolen: " + tokens[i]);    
                        //}
                        
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

            final double averageProbability = calculateAverage(probabilityList);
            if ((averageProbability >= probabilityThreshold)) {
                data.setAverageProbability(averageProbability);
                data.setModel(model.getName());
                matches.add(data);
            }
            
            // Special processing
            if (specialCase && specialCaseData != null) {
                matches.add(specialCaseData);
                specialCaseData = null;
            }
        }
        
        return matches;
    }
    
    private Object callExtention(final String function, MatchMetaData data, String text)
        throws SQLException,
               NoSuchMethodException,
               SecurityException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException {
        
        if (function == null || function.equals("")) {
            log.warn("Function " + function + " is not defined");
            return null;
        } 
        
        Object value = null;
        
        try {
            final String className = Utils.getClassName(function);
            final String methodName = Utils.getMethodName(function);
            final Class<?> clazz = Class.forName(className);
            final Method method = Class.forName(className).getMethod(methodName, new Class[]{MatchMetaData.class, String.class});
            
            final SpecialCase instance = (SpecialCase) Class.forName(className).newInstance();

            final Map<String, Object> paramValues = new HashMap<>(2);
            paramValues.put("metadata", data);
            paramValues.put("text", text);            
            
            value = method.invoke(instance, data, text);
            
        } catch (AnonymizerException | InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }
        
        return value;
    }  
}