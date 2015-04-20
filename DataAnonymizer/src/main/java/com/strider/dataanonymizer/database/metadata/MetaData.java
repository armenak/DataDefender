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

import static org.apache.log4j.Logger.getLogger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.strider.dataanonymizer.utils.SQLToJavaMapping;

/**
 * Class to hold common logic between different metadata implementations.
 * 
 * @author Akira Matsuo
 */
public abstract class MetaData implements IMetaData {
    private static final Logger log = getLogger(MetaData.class);
    
    private final Properties databaseProperties;
    protected final String schema;
    private final Connection connection;
    protected String columnType;

    
    public MetaData(Properties databaseProperties, Connection connection) {
        this.databaseProperties = databaseProperties;
        this.schema = databaseProperties.getProperty("schema");
        this.connection = connection;
    }
    
    @Override
    public List<MatchMetaData> getMetaData(String columnType) {
        this.columnType = columnType;
        return getMetaData();
    }

    // protected methods that allow subclasses to customize behaviour
    protected ResultSet getTableRS(DatabaseMetaData md) throws SQLException {
        return md.getTables(null, schema, null, new String[] {"TABLE"});
    }
    protected ResultSet getPKRS(DatabaseMetaData md, String tableName) throws SQLException {
        return md.getPrimaryKeys(null, schema, tableName);
    }
    protected ResultSet getColumnRS(DatabaseMetaData md, String tableName) throws SQLException {
        return md.getColumns(null, schema, tableName, null);
    }
    protected String getColumnName(ResultSet columnRS) throws SQLException {
        return columnRS.getString(4);
    }

    public List<MatchMetaData> getMetaData() {
        List<MatchMetaData> map = new ArrayList<MatchMetaData>();
        
        // Get the metadata from the the database
        try { 
            // Getting all tables name
            DatabaseMetaData md = connection.getMetaData();
            
            String schema = databaseProperties.getProperty("schema");
            log.info("Fetching table names from schema " + schema);
            try (ResultSet tableRS = getTableRS(md)) {
                while (tableRS.next()) {
                    String tableName = tableRS.getString(3);
                    log.info("Processing table " + tableName);
                    List<String> pkeys = new ArrayList<>();
                    try (ResultSet pkRS = getPKRS(md, tableName)) {
                        while (pkRS.next()) {
                            String pkey = pkRS.getString(4);
                            log.info("PK: " + pkey);
                            pkeys.add(pkey);
                        }
                    }
                    
                    
                    try (ResultSet columnRS = getColumnRS(md, tableName)) {
                        while (columnRS.next()) {
                            String columnName = getColumnName(columnRS);
                            String colType = getColumnType(columnRS);
                            map.add(new MatchMetaData(schema, tableName, pkeys, columnName, colType));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.toString());
        }
        
        return map;
    }

    String getColumnType(ResultSet columnRS) throws SQLException {
        String colType = columnRS.getString(6);
        if (this.columnType != null && SQLToJavaMapping.isString(colType)) {
            colType = "String";
        }
        return colType;
    }
}
