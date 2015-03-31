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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.strider.dataanonymizer.database.DBConnectionFactory;
import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.utils.SQLToJavaMapping;

import static org.apache.log4j.Logger.getLogger;

/**
 * Class to hold common logic between different metadata implementations.
 * 
 * @author Akira Matsuo
 */
public abstract class MetaData implements IMetaData {
    private static final Logger log = getLogger(MetaData.class);
    
    private final Properties databaseProperties;
    private String columnType;
    
    public MetaData(Properties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }
    
    @Override
    public List<ColumnMetaData> getMetaData(String columnType) {
        this.columnType = columnType;
        return getMetaData();
    }
    
    /**
     * Helper method to return connection to allow unit testing.  
     * @param databaseProperties
     * @return db connection
     * @throws DatabaseAnonymizerException
     */
    protected Connection getConnection() throws DatabaseAnonymizerException {
        return DBConnectionFactory.createDBConnection(databaseProperties).connect();
    }
    // protected methods that allow subclasses to customize behaviour
    protected ResultSet getTableRS(DatabaseMetaData md) throws SQLException {
        return md.getTables(null, databaseProperties.getProperty("schema"), null, new String[] {"TABLE"});
    }
    protected ResultSet getColumnRS(DatabaseMetaData md, String tableName) throws SQLException {
        return md.getColumns(null, databaseProperties.getProperty("schema"), tableName, null);
    }
    protected String getColumnName(ResultSet columnRS) throws SQLException {
        return columnRS.getString(4);
    }

    
    public List<ColumnMetaData> getMetaData() {
        List<ColumnMetaData> map = new ArrayList<ColumnMetaData>();
        
        // Get the metadata from the the database
        try (Connection connection = getConnection()) { 
            // Getting all tables name
            DatabaseMetaData md = connection.getMetaData();
            String schema = databaseProperties.getProperty("schema");
            log.info("Fetching table names from schema " + schema);
            try (ResultSet tableRS = getTableRS(md)) {
                while (tableRS.next()) {
                    String tableName = tableRS.getString(3);
                    log.info("Processing table " + tableName);
                    
                    try (ResultSet columnRS = md.getColumns(null, schema, tableName, null)) {
                        while (columnRS.next()) {
                            String columnName = getColumnName(columnRS);
                            String colType = columnRS.getString(6);
                            if (this.columnType != null && SQLToJavaMapping.isString(colType)) {
                                colType = "String";
                            }
                            map.add(new ColumnMetaData(tableName, columnName, colType));
                        }
                    }
                }
            }
        } catch (SQLException | DatabaseAnonymizerException e) {
            log.error(e.toString());
        }
        
        return map;
    }
}
