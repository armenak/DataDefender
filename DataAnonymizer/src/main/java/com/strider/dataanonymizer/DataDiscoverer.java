package com.strider.dataanonymizer;

import static java.lang.Double.parseDouble;
import static java.lang.Class.forName;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static java.sql.DriverManager.getConnection;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import static com.strider.dataanonymizer.utils.AppProperties.loadPropertiesFromClassPath;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author Armenak Grigoryan
 */
public class DataDiscoverer implements IDiscoverer {
    
    private static Logger log = getLogger(ColumnDiscoverer.class);

    @Override
    public void discover(String databasePropertyFile) throws AnonymizerException {
        // Reading anonymizer.properties file
        Properties anonymizerProperties = loadPropertiesFromClassPath("anonymizer.properties");
        if (anonymizerProperties == null) {
            try {
                throw new AnonymizerException("ERROR: Column property file is not defined.");
            } catch (AnonymizerException ex) {
                log.error(ex.toString());
            }
        }
        double probabilityThreshold = parseDouble(anonymizerProperties.getProperty("probability_threshold"));
        
        // Reading database configuration file
        Configuration configuration = null;
        try {
            configuration = new PropertiesConfiguration(databasePropertyFile);
        } catch (ConfigurationException ex) {
            log.error(ColumnDiscoverer.class);
        }
        
        String driver = configuration.getString("driver");
        String database = configuration.getString("database");
        String url = configuration.getString("url");
        String userName = configuration.getString("username");
        String password = configuration.getString("password");
        log.debug("Using driver " + driver);
        log.debug("Database type: " + database);
        log.debug("Database URL: " + url);
        log.debug("Logging in using username " + userName);

        log.info("Connecting to database");
        Connection connection = null;
        try {
            forName(driver).newInstance();
            connection = getConnection(url,userName,password);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    log.error("Problem connecting to database.\n" + sqle.toString(), sqle);
                    throw new AnonymizerException(sqle.toString());                
                }
            }
            log.error("Problem connecting to database.\n" + e.toString(), e);
            throw new AnonymizerException(e.toString());
        }        
        
        // Get the metadata from the the database
        List<ColumnMetaData> map = new ArrayList<>();
        try {
            // Getting all tables name
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String tableName = rs.getString(3);
                ResultSet resultSet = md.getColumns(null, null, tableName, null);        
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    if (resultSet.getInt(5) == java.sql.Types.VARCHAR) {
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
            modelInToken = new FileInputStream("/Users/sdi/work/strider/DataAnonymizer/DataAnonymizer/src/main/resources/en-token.bin");
            modelIn = new FileInputStream("/Users/sdi/work/strider/DataAnonymizer/DataAnonymizer/src/main/resources/en-ner-person.bin");            
            
            modelToken = new TokenizerModel(modelInToken);
            tokenizer = new TokenizerME(modelToken);            
            
            model = new TokenNameFinderModel(modelIn);
            nameFinder = new NameFinderME(model);            
        } catch (FileNotFoundException ex) {
            log.error(ex.toString());
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
                ResultSet rs = null;
                try {
                    stmt = connection.createStatement();
                    final String selectStmt = "SELECT " + columnName + " FROM " + tableName;
                    rs = stmt.executeQuery(selectStmt);
                    while (rs.next()) {
                        String sentence = rs.getString(1);
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
                    rs.close();
                    stmt.close();
                } catch (SQLException sqle) {
                    log.error(sqle.toString());
                }
                
                double averageProbability = calculateAverage(probabilityList);
                if ((averageProbability >= probabilityThreshold) && (averageProbability <= 0.90 )) {
                    log.info("Probability for " + tableName + "." + columnName + " is " + averageProbability );
                }
            }
        }
    }
    
    @Override
    public void discover(String databasePropertyFile, String columnPropertyFile) {
        return;
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