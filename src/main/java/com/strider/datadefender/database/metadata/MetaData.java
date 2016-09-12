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
package com.strider.datadefender.database.metadata;

import com.strider.datadefender.utils.SQLToJavaMapping;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


/**
 * Class to hold common logic between different metadata implementations.
 * 
 * @author Akira Matsuo
 */
public abstract class MetaData implements IMetaData {
    private static final Logger log = getLogger(MetaData.class);
    
    private final Properties databaseProperties;
    private final Connection connection;
    
    protected String schema;    
    protected String columnType;

    
    public MetaData(final Properties databaseProperties, final Connection connection) {
        this.databaseProperties = databaseProperties;
        this.schema = databaseProperties.getProperty("schema");
        this.connection = connection;
    }
    
    @Override
    public List<MatchMetaData> getMetaData(final String columnType) {
        this.columnType = columnType;
        return getMetaData();
    }

    // protected methods that allow subclasses to customize behaviour
    protected ResultSet getTableRS(final DatabaseMetaData md) throws SQLException {
        return md.getTables(null, schema, null, new String[] {"TABLE"});
    }
    
    protected ResultSet getPKRS(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getPrimaryKeys(null, schema, tableName);
    }
    
    protected ResultSet getColumnRS(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getColumns(null, schema, tableName, null);
    }
    
    protected String getColumnName(final ResultSet columnRS) throws SQLException {
        return columnRS.getString(4);
    }

    @Override
    public List<MatchMetaData> getMetaData() {
        final List<MatchMetaData> map = new ArrayList<>();
        
        // Get the metadata from the the database
        try { 
            // Getting all tables name
            final DatabaseMetaData md = connection.getMetaData();
            final String schemaName = databaseProperties.getProperty("schema");
            final String skipEmptyTables = databaseProperties.getProperty("skip-empty-tables");
            
            // Populate list of tables excluded from the analysis
            List<String> excludeTablesList = new ArrayList<>();
            final String excludeTables = databaseProperties.getProperty("exclude-tables");
            if (excludeTables != null && !"".equals(excludeTables)) {
                excludeTablesList = Arrays.asList(excludeTables.split(","));
            }
            
            log.info("Fetching table names from schema " + schemaName);
            try (ResultSet tableRS = getTableRS(md)) {
                while (tableRS.next()) {
                    final String tableName = tableRS.getString(3);
                    log.debug(tableName);
                    if (excludeTablesList.contains(tableName)) {
                        log.info("Excluding table " + tableName);
                        continue;
                    }
                    // Skip table if it is empty
                    String schemaTableName = null;
                    if (schemaName != null && !schemaName.equals("")) {
                        schemaTableName = schemaName + "." + tableName;
                    }
                    if ( (skipEmptyTables != null && skipEmptyTables.equals("true")) && (getRowNumber(schemaTableName) == 0) ) {
                        log.info("Skipping empty table " + tableName);
                        continue;
                    }
                    log.info("Table " + tableName);
                    
                    List<String> pkeys = new ArrayList<>();
                    try (ResultSet pkRS = getPKRS(md, tableName)) {
                        while (pkRS.next()) {
                            String pkey = pkRS.getString(4);
                            log.debug("PK: " + pkey);
                            pkeys.add(pkey);
                        }
                    }
                    
                    
                    try (ResultSet columnRS = getColumnRS(md, tableName)) {
                        while (columnRS.next()) {
                            map.add(new MatchMetaData(schemaName, 
                                    tableName, 
                                    pkeys, 
                                    getColumnName(columnRS), 
                                    getColumnType(columnRS), 
                                    getColumnSize(columnRS)));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.toString());
        }
        
        return map;
    }
    
    @Override
    public List<MatchMetaData> getMetaDataForRs(final ResultSet rs) throws SQLException {
        List<MatchMetaData> map = new ArrayList<>();
        
        final ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
            String colType = rsmd.getColumnTypeName(i);
            if (SQLToJavaMapping.isString(colType)) {
                colType = "String";
            }
            map.add(
                new MatchMetaData(
                    rsmd.getSchemaName(i),
                    rsmd.getTableName(i),
                    null,
                    rsmd.getColumnName(i),
                    colType,
                    rsmd.getColumnDisplaySize(i)
                )
            );
        }
        return map;
    }

    protected String getColumnType(final ResultSet columnRS) throws SQLException {
        String colType = columnRS.getString(6);
        if (this.columnType != null && SQLToJavaMapping.isString(colType)) {
            colType = "String";
        }
        return colType;
    }
    
    protected int getColumnSize(final ResultSet columnRs) throws SQLException {
        return columnRs.getInt(7);
    }
    
    private int getRowNumber(final String table) {
        int rowNum = 0;
                
        try (Statement stmt = connection.createStatement();) {
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + table);) {
                rs.next();
                rowNum = rs.getInt(1);
            }
        } catch (SQLException sqle) {
            log.error(sqle.toString());
        }
        
        return rowNum;
    }
}
