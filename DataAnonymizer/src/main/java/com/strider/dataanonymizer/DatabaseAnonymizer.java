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

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;


/**
 *
 * @author strider
 */
public class DatabaseAnonymizer implements IAnonymizer { 
    
    static Logger log = Logger.getLogger(DatabaseAnonymizer.class);

    @Override
    public void anonymize(String propertyFile) {

        // Reading configuration file
        Configuration configuration = null;
        try {
            configuration = new PropertiesConfiguration(propertyFile);
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
        } catch (Exception e) {
            log.error("Problem connecting to database.\n" + e.toString(), e);
        }        
        
        // Get the metadata from the the database
        List<Pair> map = new ArrayList<Pair>();
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
                    System.out.println("table:"+tableName+" column:"+columnName);
                }
            }
        } catch (SQLException e) {
            log.error(e);
        }
        
        // Now we collect data from the requirement
        
        
        
        log.info(map.toString());
        
        
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
        
        public String toString() {
            return this.tableName + "->" + this.columnName;
        }
    }
}