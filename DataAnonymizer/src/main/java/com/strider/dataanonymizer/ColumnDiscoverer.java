package com.strider.dataanonymizer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

/**
 *
 * @author strider
 */
public class ColumnDiscoverer implements IDiscoverer { 
    
    static Logger log = Logger.getLogger(ColumnDiscoverer.class);

    @Override
    public void discover(String databasePropertyFile, String columnPropertyFile) {

        // Reading configuration file
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
            Class.forName(driver).newInstance();
            connection = DriverManager.getConnection(url,userName,password);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException e) {
            log.error("Problem connecting to database.\n" + e.toString(), e);
        }        
        
        // Get the metadata from the the database
        List<Pair> map = new ArrayList<>();
        try {
            // Getting all tables name
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String tableName = rs.getString(3);
                ResultSet resultSet = md.getColumns(null, null, tableName, null);        
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    map.add(new Pair(tableName, columnName));
                }
            }
        } catch (SQLException e) {
            log.error(e);
        }
                
        // Get the list of "suspicios" field names from property file
        // Reading configuration file
        Configuration columnsConfiguration = null;
        try {
            columnsConfiguration = new PropertiesConfiguration("columns.properties");
        } catch (ConfigurationException ex) {
            log.error(ColumnDiscoverer.class);
        }        
        Iterator<String> iterator = columnsConfiguration.getKeys();
        List<String> suspList = IteratorUtils.toList(iterator);          
        
        
        ArrayList<String> matches = new ArrayList<>();
        for(String s: suspList) {
            Pattern p = Pattern.compile(s);
            // Find out if database columns contain any of of the "suspicios" fields
            for(Pair pair: map) {
                String tableName = pair.getTableName();
                String columnName = pair.getColumnName();
                if (p.matcher(columnName).matches()) {
                    matches.add(tableName + "." + columnName);
                }                            
            }            
        }
        
        // Report column names
        log.info("-----------------");        
        log.info("List of suspects:");
        log.info("-----------------");
        for (String entry: matches) {
            log.info(entry);
        }
    }
    
    private class Pair {
        private String tableName;
        private String columnName;

        Pair(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }
        
        public String getTableName() {
            return this.tableName;
        }
        
        public String getColumnName() {
            return this.columnName;
        }
        
        @Override
        public String toString() {
            return this.tableName + "." + this.columnName;
        }
    }
}