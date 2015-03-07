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
package com.strider.dataanonymizer.database.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import com.strider.dataanonymizer.database.DBConnectionFactory;
import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.database.IDBConnection;
import com.strider.dataanonymizer.utils.SQLToJavaMapping;

/**
 * @author armenak
 */
public class OracleMetaData implements IMetaData {
    
    private static final Logger log = getLogger(OracleMetaData.class);
    
    private Properties databaseProperties = null;
    private String columnType = null;
    
    public OracleMetaData(final Properties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }
    
    @Override
    public List<ColumnMetaData> getMetaData(String columnType) {
        this.columnType = columnType;
        return this.getMetaData();
    }
    
    @Override
    public List<ColumnMetaData> getMetaData() {
        List<ColumnMetaData> map = new ArrayList();
        
        String schema = databaseProperties.getProperty("schema");        
        
        IDBConnection dbConnection;
        Connection connection = null;
        try {
            dbConnection = DBConnectionFactory.createDBConnection(databaseProperties);
            connection = dbConnection.connect(databaseProperties);
        } catch (DatabaseAnonymizerException ex) {
            log.info(ex.toString());
        }

        ResultSet rs = null;
        // Get the metadata from the the database
        try {
            // Getting all tables name
            DatabaseMetaData md = connection.getMetaData();
            log.info("Fetching table names from schema " + schema); 
            rs = md.getTables(null, schema, null, new String[] {"TABLE"});
            
            while (rs.next()) {
                String tableName = rs.getString(3);
                log.info("Processing table " + tableName); 
                ResultSet resultSet = null;
                
                resultSet = md.getColumns(null, schema, tableName, null);
                while (resultSet.next()) {
                    String columnName = resultSet.getString(4);
                    String colType = resultSet.getString(6);
                    if (this.columnType != null) {
                        if (SQLToJavaMapping.isString(colType)) {
                            colType = "String";
                        }
                    }                    
                    map.add(new ColumnMetaData(tableName, columnName, colType));
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
        
        return map;
    }
}