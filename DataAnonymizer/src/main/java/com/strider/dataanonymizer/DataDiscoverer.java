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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.log4j.Logger;

import com.strider.dataanonymizer.database.IDBFactory;
import com.strider.dataanonymizer.database.metadata.ColumnMetaData;
import com.strider.dataanonymizer.database.metadata.IMetaData;
import com.strider.dataanonymizer.database.sqlbuilder.ISQLBuilder;
import com.strider.dataanonymizer.utils.CommonUtils;
import com.strider.dataanonymizer.utils.SQLToJavaMapping;

/**
 *
 * @author Armenak Grigoryan
 */
public class DataDiscoverer implements IDiscoverer {
    
    private static Logger log = getLogger(DataDiscoverer.class);
//    TODO: Uncomment back in if/when we need it
//    private static List firstAndLastNames = new ArrayList();
    
    
    @Override
    public void discover(IDBFactory factory, Properties dataDiscoveryProperties, Collection<String> tables) 
    throws AnonymizerException {
        log.info("Data discovery in process");

        double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        
        IMetaData metaData = factory.fetchMetaData();
        List<ColumnMetaData> map = metaData.getMetaData();    
       
        InputStream modelInToken = null;
        InputStream modelIn = null;        
        TokenizerModel modelToken = null;
        Tokenizer tokenizer = null;
        
        TokenNameFinderModel model = null;
        NameFinderME nameFinder = null;
        
        try {
        
//            firstAndLastNames = CommonUtils.readStreamOfLines(dataDiscoveryProperties.getProperty("names"));

            modelInToken = new FileInputStream(dataDiscoveryProperties.getProperty("english_tokens"));
            modelIn = new FileInputStream(dataDiscoveryProperties.getProperty("english_ner_person"));            
            
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
        Connection connection = factory.createDBConnection().connect();
        ISQLBuilder sqlBuilder = factory.createSQLBuilder();

        // Start running NLP algorithms for each column and collct percentage
        log.info("List of suspects:");
        log.info(String.format("%20s %20s %20s", "Table*", "Column*", "Probability*"));
        
        for(ColumnMetaData pair: map) {
            if (SQLToJavaMapping.isString(pair.getColumnType())) {
                String tableName = pair.getTableName();
                String columnName = pair.getColumnName();           
                List<Double> probabilityList = new ArrayList<>();
                
                if (!tables.isEmpty() && !tables.contains(tableName.toLowerCase())) {
                    continue;
                }
                
                String tableNamePattern = dataDiscoveryProperties.getProperty("table_name_pattern");
                if (!CommonUtils.isEmptyString(tableNamePattern)) {
                    Pattern p = compile(tableNamePattern);
                    if (!p.matcher(tableName).matches()) {
                        continue;
                    }
                }

                Statement stmt = null;
                ResultSet resultSet = null;
                try {
                    stmt = connection.createStatement();
                    String table = sqlBuilder.prefixSchema(tableName);
                    
                    int limit = Integer.parseInt(dataDiscoveryProperties.getProperty("limit"));
                    String query = sqlBuilder.buildSelectWithLimit(
                            "SELECT " + columnName + 
                            " FROM " + table + 
                            " WHERE " + columnName  + " IS NOT NULL ", limit);
                    
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        String sentence = resultSet.getString(1);
                        if (sentence != null && !sentence.isEmpty()) {
                            // Convert sentence into tokens
                            String tokens[] = tokenizer.tokenize(sentence);
                            // Find names
                            Span nameSpans[] = nameFinder.find(tokens);
                            //find probabilities for names
                            double[] spanProbs = nameFinder.probs(nameSpans);
                            //display names
                            for( int i = 0; i<nameSpans.length; i++) {
                                probabilityList.add(spanProbs[i]);
                            }
                            
                            // Now let's try to find first or last name
                            //if (firstAndLastNames.contains(sentence.toUpperCase())) {
                            //    probabilityList.add(0.95);
                            //}
                        }
                    }
                    resultSet.close();
                    stmt.close();
                } catch (SQLException sqle) {
                    try {
                        if (stmt != null) {
                            stmt.close();
                        }
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    } catch (SQLException sql) {
                        log.error(sql.toString());
                    }
                    log.error(sqle.toString());
                }
                
                double averageProbability = calculateAverage(probabilityList);
                if ((averageProbability >= probabilityThreshold) && (averageProbability <= 0.99 )) {
                    String probability = new DecimalFormat("#.##").format(averageProbability);
                    log.info(String.format("%20s %20s %20s", tableName, columnName, probability));
                }
            }
        }
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