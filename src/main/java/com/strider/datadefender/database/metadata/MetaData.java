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

import com.strider.datadefender.utils.CommonUtils;
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
import java.util.Set;

import org.apache.log4j.Logger;

import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.utils.SQLToJavaMapping;
import java.util.Locale;

/**
 * Class to hold common logic between different metadata implementations.
 *
 * @author Akira Matsuo
 */
public abstract class MetaData implements IMetaData {
    private static final Logger log = getLogger(MetaData.class);
    private final Properties    databaseProperties;
    private final Connection    connection;
    protected String            schema;
    protected String            columnType;

    public MetaData(final Properties databaseProperties, final Connection connection) {
        this.databaseProperties = databaseProperties;
        this.schema             = databaseProperties.getProperty("schema");
        this.connection         = connection;
    }

    private boolean containsCaseInsensitive(String s, Set<String> set) {
        return set.stream().anyMatch(x -> x.equalsIgnoreCase(s));
    }

    protected String getColumnName(final ResultSet columnRS) throws SQLException {
        return columnRS.getString(4);
    }

    protected ResultSet getColumnRS(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getColumns(null, schema, tableName, null);
    }

    protected int getColumnSize(final ResultSet columnRs) throws SQLException {
        return columnRs.getInt(7);
    }

    protected String getColumnType(final ResultSet columnRS) throws SQLException {
        String colType = columnRS.getString(6);

        if ((this.columnType != null) && SQLToJavaMapping.isString(colType)) {
            colType = "String";
        }

        return colType;
    }

    @Override
    public List<MatchMetaData> getMetaData(String vendor) {
        final List<MatchMetaData> map = new ArrayList<>();

        // Get the metadata from the the database
        try {

            // Getting all tables name
            final DatabaseMetaData md              = connection.getMetaData();
            final String           schemaName      = databaseProperties.getProperty("schema");
            final String           skipEmptyTables = databaseProperties.getProperty("skip-empty-tables");

            // Ignore table(s) excluded from the analysis
            List<String> excludeTablesList = new ArrayList<>();
            final String excludeTables     = databaseProperties.getProperty("exclude-tables");

            if ((excludeTables != null) &&!"".equals(excludeTables)) {
                excludeTablesList = Arrays.asList(excludeTables.split(","));
            }
            
            // Ignore table(s) excluded from the analysis
            List<String> includeTablesList = new ArrayList<>();
            final String includeTables     = databaseProperties.getProperty("include-tables");

            if ((includeTables != null) &&!"".equals(includeTables)) {
                includeTablesList = Arrays.asList(includeTables.split(","));
            }            

            log.info("Fetching table names from schema " + schemaName);

            try (ResultSet tableRS = getTableRS(md)) {
                while (tableRS.next()) {
                    final String tableName = tableRS.getString(3);

                    log.info("Processing table [" +tableName +"]");
                    
                    // Exception for PostgreSQL
                    
                    if (vendor.equals("postgresql") && tableName.contains("sql_")) {
                        log.info("Skipping " + tableName);
                        continue;
                    }
                    
                    
                    if (excludeTablesList.size() > 0) {
                        log.debug("Excluded table list: " + excludeTablesList.toString());
                        List matchingList = CommonUtils.getMatchingStrings(excludeTablesList, tableName.toUpperCase(Locale.ENGLISH));
                        log.debug("matchingList: [" + matchingList + "]");
                        if (matchingList != null && !matchingList.isEmpty()) {
                            log.info("Excluding table " + tableName);
                            continue;
                        }
                    }

                    if (excludeTablesList.isEmpty() && includeTablesList.size() > 0) {
                        log.debug("Include table list: " + includeTablesList.toString());
                        List matchingList = CommonUtils.getMatchingStrings(includeTablesList, tableName.toUpperCase(Locale.ENGLISH));
                        log.debug("matchingList: [" + matchingList + "]");
                        if (matchingList == null || matchingList.isEmpty()) {
                            log.info("This table is not in include-table list " + tableName);

                            continue;
                        }
                    }                    
                    
                    String schemaTableName = null;

                    if ((schemaName != null) &&!schemaName.equals("")) {
                        schemaTableName = schemaName + "." + tableName;
                    }

                    if (((skipEmptyTables != null) && skipEmptyTables.equals("true"))
                            && (getRowNumber(schemaTableName) == 0)) {
                        log.info("Skipping empty table " + tableName);

                        continue;
                    }

                    // Retrieve primary keys
                    final List<String> pKeys = new ArrayList<>();

                    try (ResultSet pkRS = getPKRS(md, tableName)) {
                        while (pkRS.next()) {
                            final String pkey = pkRS.getString(4);

                            log.debug("PK: " + pkey);
                            pKeys.add(pkey.toLowerCase(Locale.ENGLISH));
                        }
                    }
                    
                    // Retrieve foreign keys
                    final List<String> fKeys = new ArrayList<>();

                    try (ResultSet pkRS = getPKRS(md, tableName)) {
                        while (pkRS.next()) {
                            final String fKey = pkRS.getString(4);

                            log.debug("PK: " + fKey);
                            fKeys.add(fKey.toLowerCase(Locale.ENGLISH));
                        }
                    }                    

                    try (ResultSet columnRS = getColumnRS(md, tableName)) {
                        log.debug(tableName);

                        while (columnRS.next()) {
                            log.debug(getColumnName(columnRS));
                            map.add(new MatchMetaData(schemaName,
                                                      tableName,
                                                      pKeys,
                                                      fKeys,
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
        final List<MatchMetaData> map  = new ArrayList<>();
        final ResultSetMetaData   rsmd = rs.getMetaData();

        for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
            String colType = rsmd.getColumnTypeName(i);

            if (SQLToJavaMapping.isString(colType)) {
                colType = "String";
            }

            map.add(new MatchMetaData(rsmd.getSchemaName(i),
                                      rsmd.getTableName(i),
                                      null,
                                      null,
                                      rsmd.getColumnName(i),
                                      colType,
                                      rsmd.getColumnDisplaySize(i)));
        }

        return map;
    }

    protected ResultSet getPKRS(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getPrimaryKeys(null, schema, tableName);
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

//  @Override
//  public List<MatchMetaData> getMetaData(final String columnType) {
//      this.columnType = columnType;
//      return getMetaData();
//  }
    // protected methods that allow subclasses to customize behaviour
    protected ResultSet getTableRS(final DatabaseMetaData md) throws SQLException {
        return md.getTables(null, schema, null, new String[] { "TABLE" });
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
