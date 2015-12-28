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

package com.strider.dataanonymizer;

import static java.lang.Double.parseDouble;
import static java.util.regex.Pattern.compile;
import static org.apache.log4j.Logger.getLogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.log4j.Logger;

import com.strider.dataanonymizer.database.IDBFactory;
import com.strider.dataanonymizer.database.metadata.IMetaData;
import com.strider.dataanonymizer.database.metadata.MatchMetaData;
import com.strider.dataanonymizer.database.sqlbuilder.ISQLBuilder;
import com.strider.dataanonymizer.utils.CommonUtils;
import com.strider.dataanonymizer.utils.SQLToJavaMapping;


/**
 *
 * @author Armenak Grigoryan
 */
public class DataDiscoverer extends Discoverer {
    
    private static Logger log = getLogger(DataDiscoverer.class);
    
    private final static String PERSON_MODEL   = "person";
    private final static String LOCATION_MODEL = "location";
    private final static String DATE_MODEL     = "date";
    private final static String TIME_MODEL     = "time";
    private final static String MONEY_MODEL    = "money";    
    
    @Override
    public List<MatchMetaData> discover(IDBFactory factory, Properties dataDiscoveryProperties, Set<String> tables) 
    throws AnonymizerException {
        log.info("Data discovery in process");

        // Get the probability threshold from property file
        double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        log.info("Probability threshold = " + probabilityThreshold);
        
        // Creatting Person model and running discovery
        Model modelPerson = createModel(dataDiscoveryProperties, PERSON_MODEL);
        matches = discoverAgainstSingleModel(factory, dataDiscoveryProperties, tables, modelPerson, probabilityThreshold);
        
        // Creatting Location model and running discovery
        Model modelLocation = createModel(dataDiscoveryProperties, LOCATION_MODEL);
        matches = discoverAgainstSingleModel(factory, dataDiscoveryProperties, tables, modelLocation, probabilityThreshold);        
               
        return matches;
    }
    
    private List<MatchMetaData> discoverAgainstSingleModel(IDBFactory factory, Properties dataDiscoveryProperties, 
            Set<String> tables, Model model, double probabilityThreshold)
    throws AnonymizerException {
        IMetaData metaData = factory.fetchMetaData();
        List<MatchMetaData> map = metaData.getMetaData();
        // Start running NLP algorithms for each column and collect percentage
        log.info("List of suspects:");
        log.info(String.format("%20s %20s %20s", "Table*", "Column*", "Probability*"));
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        matches = new ArrayList<>();

        ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        for(MatchMetaData data: map) {
            if (SQLToJavaMapping.isString(data.getColumnType())) {
                String tableName = data.getTableName();
                String columnName = data.getColumnName();
                List<Double> probabilityList = new ArrayList<>();
                
                log.debug("Analyzing table [" + tableName + "]");
                
                if (!tables.isEmpty() && !tables.contains(tableName.toLowerCase())) {
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
                
                String table = sqlBuilder.prefixSchema(tableName);
                int limit = Integer.parseInt(dataDiscoveryProperties.getProperty("limit"));
                String query = sqlBuilder.buildSelectWithLimit(
                    "SELECT " + columnName + 
                    " FROM " + table + 
                    " WHERE " + columnName  + " IS NOT NULL ", limit);
                log.debug("Executing query against database: " + query);
                
                try (Statement stmt = factory.getConnection().createStatement();
                    ResultSet resultSet = stmt.executeQuery(query);) {

                    while (resultSet.next()) {
                        String sentence = resultSet.getString(1);
                        if (sentence != null && !sentence.isEmpty()) {
                            // Convert sentence into tokens
                            String tokens[] = model.getTokenizer().tokenize(sentence);
                            // Find names
                            Span nameSpans[] = model.getNameFinder().find(tokens);
                            //find probabilities for names
                            double[] spanProbs = model.getNameFinder().probs(nameSpans);
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
                    String probability = decimalFormat.format(averageProbability);
                    String result = String.format("%20s %20s %20s %20s", tableName, columnName, probability, model.getName());
                    log.info(result);
                    matches.add(data);
                }
            }
        }
        return matches;
    }
    
    /**
     * Creates model POJO based on OpenNLP model
     * 
     * @param dataDiscoveryProperties
     * @param modelType
     * @return Model
     */
    private Model createModel(Properties dataDiscoveryProperties, String modelName) {
        InputStream modelInToken = null;
        InputStream modelIn = null;        
        TokenizerModel modelToken = null;
        Tokenizer tokenizer = null;
        
        TokenNameFinderModel model = null;
        NameFinderME nameFinder = null;
        
        try {
            modelInToken = new FileInputStream(dataDiscoveryProperties.getProperty("english_tokens"));
            modelIn = new FileInputStream(dataDiscoveryProperties.getProperty(modelName));            
            
            modelToken = new TokenizerModel(modelInToken);
            tokenizer = new TokenizerME(modelToken);            
            
            model = new TokenNameFinderModel(modelIn);
            nameFinder = new NameFinderME(model);    
            
            modelInToken.close();
            modelIn.close();
        } catch (FileNotFoundException ex) {
            log.error(ex.toString());
            try {
                if (modelInToken != null) {
                    modelInToken.close();
                }
                if (modelIn != null) {
                    modelIn.close();
                }
            } catch (IOException ioe) {
                log.error(ioe.toString());
            }
        } catch (IOException ex) {
            log.error(ex.toString());
        }
        
        return new Model(tokenizer, nameFinder, modelName);
    }
    
    private double calculateAverage(List <Double> values) {
        Double sum = 0.0;
        if(!values.isEmpty()) {
            for (Double value : values) {
                sum += value;
            }
            return sum / values.size();
        }
        return sum;
    }    
}