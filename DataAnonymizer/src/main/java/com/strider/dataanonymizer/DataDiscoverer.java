/*
 * 
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
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

import com.strider.dataanonymizer.database.metadata.ColumnMetaData;
import com.strider.dataanonymizer.database.DBConnectionFactory;
import com.strider.dataanonymizer.database.IDBConnection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Double.parseDouble;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author Armenak Grigoryan
 */
public class DataDiscoverer implements IDiscoverer {
    
    private static Logger log = getLogger(DataDiscoverer.class);

    @Override
    public void discover(Properties databaseProperties, Properties dataDiscoveryProperties) throws AnonymizerException {
        double probabilityThreshold = parseDouble(dataDiscoveryProperties.getProperty("probability_threshold"));
        
        log.info("Connecting to database");        
        IDBConnection dbConnection = DBConnectionFactory.createDBConnection(databaseProperties);
        Connection connection = dbConnection.connect(databaseProperties);
        
        String vendor = databaseProperties.getProperty("vendor");
        String schema = databaseProperties.getProperty("schema");        
        
        ResultSet rs = null;
        // Get the metadata from the the database
        List<ColumnMetaData> map = new ArrayList<>();
        try {
            // Getting all tables name
            DatabaseMetaData md = connection.getMetaData();
            if (vendor.equals("mssql")) {
                rs = md.getTables(null, null, null, new String[] {"TABLE"});
            } else {
                rs = md.getTables(null, null, "%", null);
            }            
            while (rs.next()) {
                String tableName = rs.getString(3);
                ResultSet resultSet = null;   
                if (vendor.equals("mssql")) {
                    resultSet = md.getColumns(null, schema, tableName, null);
                } else {
                    resultSet = md.getColumns(null, null, tableName, null);
                }                
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    log.info(columnName + ":" + resultSet.getInt(5));
                    if ((resultSet.getInt(5) == java.sql.Types.VARCHAR) || 
                        (resultSet.getInt(5) == java.sql.Types.NVARCHAR)) {
                        ColumnMetaData columnMetaData = new ColumnMetaData(tableName, columnName, "String");
                        map.add(columnMetaData);                        
                    }
                }
            }
            rs.close();
        } catch (SQLException e) {
            log.error(e);
        }  
        
        InputStream modelInToken = null;
        InputStream modelIn = null;        
        TokenizerModel modelToken = null;
        Tokenizer tokenizer = null;
        
        TokenNameFinderModel model = null;
        NameFinderME nameFinder = null;
        
        try {
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
        
        // Start running NLP algorithms for each column and collct percentage
        log.info("List of suspects:");
        log.info("-----------------");
        for(ColumnMetaData pair: map) {
            if (pair.getColumnType().equals("String")) {
                String tableName = pair.getTableName();
                String columnName = pair.getColumnName();                
                List<Double> probabilityList = new ArrayList<>();

                Statement stmt = null;
                ResultSet resultSet = null;
                try {
                    stmt = connection.createStatement();
                    String table = tableName;
                    if (vendor.equals("mssql")) {
                        table = schema + "." + tableName;
                    }
                    String query = "SELECT " + columnName + " FROM " + table;
                    log.info(query);
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
                if ((averageProbability >= probabilityThreshold) && (averageProbability <= 0.90 )) {
                    log.info("Probability for " + tableName + "." + columnName + " is " + averageProbability );
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