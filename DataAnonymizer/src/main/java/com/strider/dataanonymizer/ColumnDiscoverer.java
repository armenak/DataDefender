package com.strider.dataanonymizer;

import com.strider.dataanonymizer.database.DBConnectionFactory;
import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.database.IDBConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.collections.IteratorUtils.toList;
import static org.apache.log4j.Logger.getLogger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

/**
 *
 * @author strider
 */
public class ColumnDiscoverer implements IDiscoverer { 
    
    private static Logger log = getLogger(ColumnDiscoverer.class);

    @Override
    public void discover(String databasePropertyFile, String columnPropertyFile) throws DatabaseAnonymizerException {

        log.info("Connecting to database");        
        IDBConnection dbConnection = DBConnectionFactory.createDBConnection(databasePropertyFile);
        Connection connection = dbConnection.connect(databasePropertyFile);
                
        ResultSet rs = null;
        // Get the metadata from the the database
        List<ColumnMetaData> map = new ArrayList<>();
        try {
            // Getting all tables name
            DatabaseMetaData md = connection.getMetaData();
            rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String tableName = rs.getString(3);
                ResultSet resultSet = md.getColumns(null, null, tableName, null);        
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    String columnType = resultSet.getString("DATA_TYPE");
                    map.add(new ColumnMetaData(tableName, columnName, columnType));
                }
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqle) {
                    log.error(sqle.toString());
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sql) {
                    log.error(sql.toString());
                }
            }            
            log.error(e.toString());
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
        List<String> suspList = toList(iterator);          
        
        ArrayList<String> matches = new ArrayList<>();
        for(String s: suspList) {
            Pattern p = compile(s);
            // Find out if database columns contain any of of the "suspicios" fields
            for(ColumnMetaData pair: map) {
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
    
    public void discover(String databasePropertyFile) throws DatabaseAnonymizerException {
        this.discover(databasePropertyFile, "column.properties");
    }
}