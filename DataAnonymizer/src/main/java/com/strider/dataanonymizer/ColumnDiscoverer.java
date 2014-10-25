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
import java.util.Properties;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.collections.IteratorUtils.toList;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


/**
 *
 * @author Armenak Grigoryan
 */
public class ColumnDiscoverer implements IDiscoverer { 
    
    private static Logger log = getLogger(ColumnDiscoverer.class);

    @Override
    public void discover(Properties databaseProperties, Properties columnProperties) throws DatabaseAnonymizerException {

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
            log.info("Fetching table names from schema " + schema); 
            if (vendor.equals("mssql")) {
                rs = md.getTables(null, schema, null, new String[] {"TABLE"});
            } else {
                rs = md.getTables(null, null, "%", null);
            }
            
            while (rs.next()) {
                String tableName = rs.getString(3);
                log.info("Processing table " + tableName); 
                ResultSet resultSet = null;
                
                log.info("Fetching columns"); 
                if (vendor.equals("mssql")) {
                    resultSet = md.getColumns("da_test", schema, tableName, null);
                } else {
                    resultSet = md.getColumns(null, null, tableName, null);
                }
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
            columnsConfiguration = new PropertiesConfiguration("columndiscovery.properties");
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
}