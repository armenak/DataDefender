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
import com.strider.datadefender.functions.Utils;
import com.strider.datadefender.report.ReportUtil;
import com.strider.datadefender.specialcase.SpecialCase;
import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.utils.Score;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 *
 * @author Armenak Grigoryan
 */
public class DatabaseDiscoverer extends Discoverer {

    private static final Logger log = getLogger(DatabaseDiscoverer.class);
    private static final String YES = "yes";

    private static String[] modelList;

    @SuppressWarnings("unchecked")
    public List<MatchMetaData> discover(final IDBFactory factory, final Properties dataDiscoveryProperties, final Set<String> tables)
    throws  ParseException, DatabaseDiscoveryException {
        log.info("Data discovery in process");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        String calculate_score = dataDiscoveryProperties.getProperty("score_calculation");
        if (CommonUtils.isEmptyString(calculate_score)) {
            calculate_score = "false";
        }

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
        final Score score = new Score();
        int highRiskColumns = 0;
        int rowCount=0;
        for(final MatchMetaData data: finalList) {
            // Row count
            if (YES.equals(calculate_score)) {
              log.debug("Skipping table rowcount...");
              rowCount = ReportUtil.rowCount(factory, 
                      data.getTableName(),
                      Integer.valueOf(dataDiscoveryProperties.getProperty("limit")));
            }

            // Getting 5 sample values
            // final List<String> sampleDataList = ReportUtil.sampleData(factory, data.getTableName(), data.getColumnName());

            // Output
            log.info("Column                      : " + data.toString());
            log.info( CommonUtils.fixedLengthString('=', data.toString().length() + 30));

            log.info("Probability                 : " + decimalFormat.format(data.getAverageProbability()));
            log.info("Model                       : " + data.getModel());
            if (YES.equals(calculate_score)) {
                log.info("Number of rows in the table : " + rowCount);
                log.info("Score                       : " + score.columnScore(rowCount));
            } else {
                log.info("Number of rows in the table : N/A");
                log.info("Score                       : N/A");
            }

            log.info("Sample data");
            log.info( CommonUtils.fixedLengthString('-', 11));

            final List<Probability> probabilityList = data.getProbabilityList();

            Collections.sort(probabilityList,
                Comparator.comparingDouble(Probability::getProbabilityValue).reversed());

            int y=0;
            if (data.getProbabilityList().size() >= 5) {
                y = 5;
            } else {
                y = data.getProbabilityList().size();
            }

            for (int i=0; i<y; i++) {
                final Probability p = data.getProbabilityList().get(i);
                log.info(p.getSentence() + ":" + p.getProbabilityValue());
            }

            log.info("" );
            
            // Score calculation is evaluated with score_calculation parameter
            if (YES.equals(calculate_score) && score.columnScore(rowCount).equals("High")) {
                highRiskColumns++;
            }
        }
        
        // Only applicable when parameter table_rowcount=yes otherwise score calculation should not be done
        if (YES.equals(calculate_score)) {
            log.info("Overall score: " + score.dataStoreScore());
            log.info("");

            if (finalList != null && finalList.size() > 0) {
                log.info("============================================");
                final int threshold_count    = Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_count"));
                if (finalList.size() > threshold_count) {
                    log.info("Number of PI [" + finalList.size() + "] columns is higher than defined threashold [" + threshold_count + "]");
                } else {
                    log.info("Number of PI [" + finalList.size() + "] columns is lower or equal than defined threashold [" + threshold_count + "]");
                }
            
                final int threshold_highrisk = Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_highrisk"));
                if (highRiskColumns > threshold_highrisk) {
                    log.info("Number of High risk PI [" + highRiskColumns + "] columns is higher than defined threashold [" + threshold_highrisk + "]");
                } else {
                    log.info("Number of High risk PI [" + highRiskColumns + "] columns is lower or equal than defined threashold [" + threshold_highrisk + "]");
                }
            }
        } else {
            log.info("Overall score: N/A");
        }

        log.info("matches: " + matches.toString());
        return matches;
    }

    private List<MatchMetaData> discoverAgainstSingleModel(final IDBFactory factory, final Properties dataDiscoveryProperties,
            final Set<String> tables, final Model model, final double probabilityThreshold)
    throws ParseException, DatabaseDiscoveryException {
        final IMetaData metaData = factory.fetchMetaData();
        final List<MatchMetaData> map = metaData.getMetaData();
        // Start running NLP algorithms for each column and collect percentage
        matches = new ArrayList<>();
        MatchMetaData specialCaseData = null;
        final List<MatchMetaData> specialCaseDataList = new ArrayList();
        boolean specialCase = false;


        final String extentionList = dataDiscoveryProperties.getProperty("extentions");
        String[] specialCaseFunctions = null;
        if (!CommonUtils.isEmptyString(extentionList)) {
            specialCaseFunctions = extentionList.split(",");
            if (specialCaseFunctions != null && specialCaseFunctions.length > 0) {
                specialCase = true;
            }
        }

        final ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        List<Probability> probabilityList;
        for(final MatchMetaData data: map) {
            final String tableName = data.getTableName();
            final String columnName = data.getColumnName();
            log.debug(data.getColumnType());
            probabilityList = new ArrayList<>();

            log.info("Analyzing table [" + tableName + "].["+ columnName+ "]");

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
                "SELECT `" + columnName + "`" +
                " FROM " + table +
                " WHERE `" + columnName  + "` IS NOT NULL ", limit);
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
                                    if (specialCaseData != null) {
                                        log.info("Adding new special case data: " + specialCaseData.toString());
                                        specialCaseDataList.add(specialCaseData);
                                    }
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
                            final DateFormat targetFormat = new SimpleDateFormat("MMM d, yy", Locale.ENGLISH);
                            final java.util.Date date = originalFormat.parse(sentence);
                            processingValue = targetFormat.format(date);
                        } else {
                            processingValue = sentence;
                        }

                        //log.debug(sentence);
                        // Convert sentence into tokens
                        final String tokens[] = model.getTokenizer().tokenize(processingValue);

                        // Find names
                        final Span nameSpans[] = model.getNameFinder().find(tokens);

                        //find probabilities for names
                        final double[] spanProbs = model.getNameFinder().probs(nameSpans);

                        // Collect top X tokens with highest probability


                        //display names
                        for( int i = 0; i<nameSpans.length; i++) {
                            final String span = nameSpans[i].toString();
                            if (span.length() >2 ) {
                                log.debug("Span: "+span);
                                log.debug("Covered text is: "+tokens[nameSpans[i].getStart()]);
                                log.debug("Probability is: "+spanProbs[i]);
                                probabilityList.add(new Probability(tokens[nameSpans[i].getStart()], spanProbs[i]));
                            }
                        }
                        // From OpenNLP documentation:
                        //  After every document clearAdaptiveData must be called to clear the adaptive data in the feature generators.
                        // Not calling clearAdaptiveData can lead to a sharp drop in the detection rate after a few documents.
                        model.getNameFinder().clearAdaptiveData();
                    }
                }
            } catch (SQLException sqle) {
                log.error(sqle.toString());
            }

            final double averageProbability = calculateAverage(probabilityList);
            if (averageProbability >= probabilityThreshold) {
                data.setAverageProbability(averageProbability);
                data.setModel(model.getName());
                data.setProbabilityList(probabilityList);
                matches.add(data);
            }
        }

        // Special processing
        if (specialCaseDataList != null && !specialCaseDataList.isEmpty()) {
            log.info("Special case data is processed :" + specialCaseDataList.toString());
            for (final MatchMetaData specialData : specialCaseDataList) {
                matches.add(specialData);
            }
        }        
        
        return matches;
    }

    /**
     * Calls a function defined as an extention
     * @param function
     * @param data
     * @param text
     * @return
     * @throws SQLException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private Object callExtention(final String function, final MatchMetaData data, final String text)
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
            final Method method = Class.forName(className).getMethod(methodName, new Class[]{MatchMetaData.class, String.class});

            final SpecialCase instance = (SpecialCase) Class.forName(className).newInstance();

            final Map<String, Object> paramValues = new HashMap<>(2);
            paramValues.put("metadata", data);
            paramValues.put("text", text);

            value = method.invoke(instance, data, text);

        } catch (InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }

        return value;
    }
}
