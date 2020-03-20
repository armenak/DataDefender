/*
 *
 * Copyright 2014-2019, Armenak Grigoryan, and individual contributors as indicated
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

import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.database.metadata.IMetaData;
import com.strider.datadefender.database.metadata.TableMetaData;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.database.sqlbuilder.ISqlBuilder;
import com.strider.datadefender.functions.Utils;
import com.strider.datadefender.report.ReportUtil;
import com.strider.datadefender.specialcase.SpecialCase;
import com.strider.datadefender.utils.Score;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import opennlp.tools.util.Span;

import static java.lang.Double.parseDouble;
import static java.util.regex.Pattern.compile;

import lombok.extern.log4j.Log4j;

/**
 *
 * @author Armenak Grigoryan
 */
@Log4j
public class DatabaseDiscoverer extends Discoverer {
    private static final String YES = "yes";
    private static String[]     modelList;

    /**
     * Calls a function defined as an extension
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
    private Object callExtension(final String function, final ColumnMetaData data, final String text)
            throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException {

        if (!StringUtils.isBlank(function)) {
            log.warn("Function {} is not defined", function);
            return null;
        }

        Object value = null;

        try {
            final String className  = Utils.getClassName(function);
            final String methodName = Utils.getMethodName(function);
            final Method method     = Class.forName(className)
                                           .getDeclaredMethod(methodName, new Class[] { ColumnMetaData.class, String.class });
            final SpecialCase         instance    = (SpecialCase) Class.forName(className).newInstance();
            value = method.invoke(instance, data, text);

        } catch (InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
            log.debug(ex.toString(), ex);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public List<ColumnMatch> discover(final IDbFactory factory,
            final Properties dataDiscoveryProperties, String vendor)
            throws ParseException, DataDefenderException, IOException, SQLException {
        log.info("Data discovery in process");

        // Get the probability threshold from property file
        final double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        String       calculate_score      = dataDiscoveryProperties.getProperty("score_calculation", "false");

        log.info("Probability threshold [{}]", probabilityThreshold);

        // Get list of models used in data discovery
        final String models = dataDiscoveryProperties.getProperty("models");

        modelList = models.split(",");
        log.info("Model list [" + Arrays.toString(modelList) + "]");

        List<ColumnMatch> finalList = new ArrayList<>();

        for (final String model : modelList) {
            log.info("********************************");
            log.info("Processing model " + model);
            log.info("********************************");

            final Model modelPerson = createModel(dataDiscoveryProperties, model);
            matches = discoverAgainstSingleModel(
                factory,
                dataDiscoveryProperties,
                modelPerson,
                probabilityThreshold
            );
            finalList = ListUtils.union(finalList, matches);
        }

        log.info("List of suspects:");

        final Score score           = new Score();
        int         highRiskColumns = 0;
        int         rowCount        = 0;

        for (final ColumnMatch match : finalList) {

            ColumnMetaData column = match.getColumn();
            // Row count
            if (YES.equals(calculate_score)) {
                log.debug("Counting number of rows ...");
                rowCount = ReportUtil.rowCount(factory, 
                               column.getTable().getTableName());
            } else {
                log.debug("Skipping counting number of rows ...");
            }

            // Getting 5 sample values
            final List<String> sampleDataList = ReportUtil.sampleData(factory, column);
            // Output
            log.info("Column                      : " + column.toString());
            log.info(StringUtils.repeat('=', column.toString().length() + 30));
            log.info("Model                       : " + match.getModel());
            log.info("Number of rows in the table : " + rowCount);

            if (YES.equals(calculate_score)) {
                log.info("Score                       : " + score.columnScore(rowCount));
            } else {
                log.info("Score                       : N/A");
            }

            log.info("Sample data");
            log.info(StringUtils.repeat('-', 11));
            
            sampleDataList.forEach((sampleData) -> {
                log.info(sampleData);
            });

            log.info("");

            // Score calculation is evaluated with score_calculation parameter
            if (YES.equals(calculate_score) && score.columnScore(rowCount).equals("High")) {
                highRiskColumns++;
            }
        }

        // Only applicable when parameter table_rowcount=yes otherwise score calculation should not be done
        if (YES.equals(calculate_score)) {
            log.info("Overall score: " + score.dataStoreScore());
            log.info("");

            if ((finalList != null) && (finalList.size() > 0)) {
                log.info("============================================");

                final int threshold_count = Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_count"));

                if (finalList.size() > threshold_count) {
                    log.info("Number of PI [" + finalList.size() + "] columns is higher than defined threashold ["
                             + threshold_count + "]");
                } else {
                    log.info("Number of PI [" + finalList.size()
                             + "] columns is lower or equal than defined threashold [" + threshold_count + "]");
                }

                final int threshold_highrisk =
                    Integer.valueOf(dataDiscoveryProperties.getProperty("threshold_highrisk"));

                if (highRiskColumns > threshold_highrisk) {
                    log.info("Number of High risk PI [" + highRiskColumns
                             + "] columns is higher than defined threashold [" + threshold_highrisk + "]");
                } else {
                    log.info("Number of High risk PI [" + highRiskColumns
                             + "] columns is lower or equal than defined threashold [" + threshold_highrisk + "]");
                }
            }
        } else {
            log.info("Overall score: N/A");
        }

        return matches;
    }

    private List<ColumnMatch> discoverAgainstSingleModel(
        final IDbFactory factory,
        final Properties dataDiscoveryProperties,
        final Model model,
        final double probabilityThreshold
    ) throws ParseException, DataDefenderException, IOException, SQLException {

        final IMetaData           metaData = factory.fetchMetaData();
        final List<TableMetaData> map      = metaData.getMetaData();

        // Start running NLP algorithms for each column and collect percentage
        matches = new ArrayList<>();

        ColumnMatch             specialCaseData;
        final List<ColumnMatch> specialCaseDataList  = new ArrayList();
        boolean                 specialCase          = false;
        final String            extensionList        = dataDiscoveryProperties.getProperty("extensions");
        String[]                specialCaseFunctions = null;

        log.info("Extension list: " + extensionList);
        
        if (StringUtils.isNotBlank(extensionList)) {
            specialCaseFunctions = extensionList.split(",");
            if ((specialCaseFunctions != null) && (specialCaseFunctions.length > 0)) {
                specialCase = true;
            }
        }

        final ISqlBuilder sqlBuilder = factory.createSQLBuilder();
        List<Probability> probabilityList;

        List<ColumnMetaData> cols = map.stream().flatMap((t) -> t.getColumns().stream()).collect(Collectors.toList());
        for (final ColumnMetaData data : cols) {
            final String tableName  = data.getTable().getTableName();
            final String columnName = data.getColumnName();
            List<ColumnMetaData> pkeys = ListUtils.emptyIfNull(data.getTable().getPrimaryKeys());
            List<ColumnMetaData> fkeys = ListUtils.emptyIfNull(data.getTable().getForeignKeys());

            log.debug("Primary keys for table {}: [{}]", () -> tableName, pkeys);
            if (pkeys.stream().anyMatch((c) -> StringUtils.equalsIgnoreCase(c.getColumnName(), columnName))) {
                log.debug("Column [" + columnName + "] is Primary Key. Skipping this column.");
                continue;
            }
            
            log.debug("Foreign key(s) for table {}: [{}]", () -> tableName, fkeys);
            if (fkeys.stream().anyMatch((c) -> StringUtils.equalsIgnoreCase(c.getColumnName(), columnName))) {
                log.debug("Column [" + columnName + "] is Foreign Key. Skipping this column.");
                continue;
            }            
            
            log.debug("Column type: [" + data.getColumnType() + "]");
            probabilityList = new ArrayList<>();
            log.info("Analyzing column [" + tableName + "].[" + columnName + "]");

            final String tableNamePattern = dataDiscoveryProperties.getProperty("table_name_pattern");

            if (StringUtils.isNotBlank(tableNamePattern)) {
                final Pattern p = compile(tableNamePattern);
                if (!p.matcher(tableName).matches()) {
                    continue;
                }
            }
            
            final String table = sqlBuilder.prefixSchema(tableName);
            final int    limit = Integer.parseInt(dataDiscoveryProperties.getProperty("limit"));
            final String query = sqlBuilder.buildSelectWithLimit(
                "SELECT " + columnName + " FROM "  + table + " WHERE "
                    + columnName + " IS NOT NULL",
                limit
            );

            log.debug("Executing query against database: " + query);

            try (Statement stmt = factory.getConnection().createStatement();
                ResultSet resultSet = stmt.executeQuery(query);) {
                while (resultSet.next()) {
                    if (Objects.equals(Blob.class, data.getColumnType())) {
                        continue;
                    }
                    if (model.getName().equals("location") && ClassUtils.isAssignable(data.getColumnType(), Number.class)) {
                        continue;
                    }

                    String sentence = "";
                    if (Objects.equals(Clob.class, data.getColumnType())) {
                        Clob clob = resultSet.getClob(1);
                        InputStream is = clob.getAsciiStream();
                        sentence = IOUtils.toString(is, StandardCharsets.UTF_8.name());
                    } else {
                        sentence = resultSet.getString(1);
                    }
                    log.debug(sentence);
                    if (specialCaseFunctions != null && specialCase) {
                        try {
                            for (String specialCaseFunction : specialCaseFunctions) {
                                if ((sentence != null) && !sentence.isEmpty()) {
                                    log.debug("sentence: " + sentence);
                                    log.debug("data: " + data);
                                    specialCaseData = (ColumnMatch) callExtension(specialCaseFunction, data, sentence);
                                    if (specialCaseData != null) {
                                        if (!specialCaseDataList.contains(specialCaseData)) {
                                            log.debug("Adding new special case data: " + specialCaseData.toString());
                                            specialCaseDataList.add(specialCaseData);
                                        }
                                    } else {
                                        log.debug("No special case data found");
                                    }
                                }
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            log.error(e.toString());
                        }
                    }
                    
                    if ((sentence != null) &&!sentence.isEmpty()) {
                        String processingValue;

                        if (Objects.equals(Date.class, data.getColumnType())
                            || Objects.equals(Timestamp.class, data.getColumnType())
                            || Objects.equals(Time.class, data.getColumnType())) {

                            final DateFormat     originalFormat = new SimpleDateFormat(sentence, Locale.ENGLISH);
                            final DateFormat     targetFormat   = new SimpleDateFormat("MMM d, yy", Locale.ENGLISH);
                            final java.util.Date date           = originalFormat.parse(sentence);

                            processingValue = targetFormat.format(date);
                        } else {
                            processingValue = sentence;
                        }

                        // LOG.debug(sentence);
                        // Convert sentence into tokens
                        final String tokens[] = model.getTokenizer().tokenize(processingValue);

                        // Find names
                        final Span nameSpans[] = model.getNameFinder().find(tokens);

                        // find probabilities for names
                        final double[] spanProbs = model.getNameFinder().probs(nameSpans);

                        // Collect top X tokens with highest probability
                        // display names
                        for (int i = 0; i < nameSpans.length; i++) {
                            final String span = nameSpans[i].toString();

                            if (span.length() > 2) {
                                log.debug("Span: " + span);
                                log.debug("Covered text is: " + tokens[nameSpans[i].getStart()]);
                                log.debug("Probability is: " + spanProbs[i]);
                                probabilityList.add(new Probability(tokens[nameSpans[i].getStart()], spanProbs[i]));
                            }
                        }

                        // From OpenNLP documentation:
                        // After every document clearAdaptiveData must be called to clear the adaptive data in the feature generators.
                        // Not calling clearAdaptiveData can lead to a sharp drop in the detection rate after a few documents.
                        model.getNameFinder().clearAdaptiveData();
                    }
                }
            } catch (SQLException sqle) {
                log.error(sqle.toString());
            }

            final double averageProbability = calculateAverage(probabilityList);

            if (averageProbability >= probabilityThreshold) {
                matches.add(new ColumnMatch(
                    data,
                    averageProbability,
                    model.getName(),
                    probabilityList)
                );
            }
        }

        // Special processing
        if (!specialCaseDataList.isEmpty()) {
            log.debug("Special case data is processed :" + specialCaseDataList.toString());

            specialCaseDataList.forEach((specialData) -> {
                matches.add(specialData);
            });
        }

        return matches;
    }
}
