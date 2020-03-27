/*
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
 */
package com.strider.datadefender.database.metadata;

import com.strider.datadefender.DbConfig;
import com.strider.datadefender.DbConfig.Vendor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Class to hold common logic between different metadata implementations.
 *
 * @author Akira Matsuo
 */
@Log4j2
public class MetaData implements IMetaData {

    private final Connection connection;
    protected final DbConfig config;
    protected final SqlTypeToClass sqlTypeMap;

    public MetaData(DbConfig config, Connection connection) {
        this(config, connection, new SqlTypeToClass());
    }

    public MetaData(DbConfig config, Connection connection, SqlTypeToClass sqlTypeMap) {
        this.config = config;
        this.connection = connection;
        this.sqlTypeMap = sqlTypeMap;
    }

    /**
     * Returns a list of metadata information for tables in the current
     * database/schema.
     *
     * @return
     */
    @Override
    public List<TableMetaData> getMetaData() throws SQLException {

        final List<TableMetaData> tables = new ArrayList<>();

        // Getting all tables name
        final DatabaseMetaData md = connection.getMetaData();
        log.info("Fetching table names");

        try (ResultSet trs = getTableResultSet(md)) {
            while (trs.next()) {
                final String tableName = trs.getString("TABLE_NAME");
                log.info("Processing table [" + tableName + "]");
                if (skipTable(tableName)) {
                    continue;
                }
                TableMetaData table = new TableMetaData(config.getSchema(), tableName);
                addColumnMetaData(md, table);
                tables.add(table);
            }
        }

        return tables;
    }

    /**
     * Returns a list of metadata information for columns in the passed
     * ResultSet.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    @Override
    public TableMetaData getMetaDataFor(final ResultSet rs) throws SQLException {
        TableMetaData table = null;
        final ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); ++i) {
            if (table == null) {
                table = new TableMetaData(rsmd.getSchemaName(i), rsmd.getTableName(i));
            }
            table.addColumn(
                rsmd.getColumnName(i),
                sqlTypeMap.getTypeFrom(rsmd.getColumnType(i)),
                rsmd.getColumnDisplaySize(i),
                false,
                null
            );
        }
        return table;
    }

    /**
     * Loops over passed patterns, and returns true if one of them matches the
     * tableName.
     *
     * @param patterns
     * @param tableName
     * @param isIncludePatterns Used for debug output (either "included" or
     *  "excluded" in output)
     * @return
     */
    private boolean checkPatternsWithTableName(List<Pattern> patterns, String tableName, boolean isIncludePatterns) {
        String upperName = tableName.toUpperCase(Locale.ENGLISH);
        boolean ret = patterns.stream().anyMatch(
            (p) -> p.matcher(upperName).matches()
        );
        String debugLogFound = (isIncludePatterns) ? "Table {} included by pattern: {}" : "Table {} excluded by pattern: {}";
        String debugLogNotFound = (isIncludePatterns) ? "Table {} did not match any inclusion patterns" : "Table {} did not match any exclusion patterns";

        log.debug(
            (ret) ? debugLogFound : debugLogNotFound,
            () -> tableName,
            () -> patterns.stream().filter(
                (p) -> p.matcher(upperName).matches()
            ).findFirst().map((p) -> p.pattern()).orElse("")
        );
        return ret;
    }

    /**
     * Returns true if the table should be included based on configured/passed
     * 'include' and 'exclude' patterns.
     *
     * @param tableName
     * @return
     */
    private boolean includeTable(String tableName) {
        List<Pattern> exclude = config.getExcludeTablePatterns();
        List<Pattern> include = config.getIncludeTablePatterns();
        if (!CollectionUtils.isEmpty(include) && !checkPatternsWithTableName(include, tableName, true)) {
            return false;
        }
        return CollectionUtils.isEmpty(exclude)
            || !checkPatternsWithTableName(exclude, tableName, false);
    }

    /**
     * Returns true if the table should be skipped for metadata extraction.
     *
     * @param tableName
     * @return 
     */
    protected boolean skipTable(String tableName) {
        if (config.getVendor() == Vendor.POSTGRESQL && tableName.startsWith("sql_")) {
            log.info("Skipping postgresql 'sql_' table: {}", tableName);
            return true;
        }
        if (!includeTable(tableName)) {
            log.info("Excluding table by inclusion/exclusion rules: {}", tableName);
            return true;
        }
        String schemaTableName = tableName;
        if (!StringUtils.isBlank(config.getSchema())) {
            schemaTableName = config.getSchema() + "." + tableName;
        }
        if (config.isSkipEmptyTables() && getNumberOfRows(schemaTableName) == 0) {
            log.info("Skipping empty table: {}", tableName);
            return true;
        }
        return false;
    }

    /**
     * Returns ResultSet for tables.
     * 
     * @param md
     * @return
     * @throws SQLException
     */
    protected ResultSet getTableResultSet(final DatabaseMetaData md) throws SQLException {
        return md.getTables(null, config.getSchema(), null, new String[] { "TABLE" });
    }

    /**
     * Performs a COUNT(*) query on the passed table to determine number of rows
     * in a table.
     *
     * @param table
     * @return
     */
    private int getNumberOfRows(final String table) {
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

    /**
     * Returns a ResultSet representing columns in the passed DatabaseMetaData
     * object.  Overridable to account for differences between databases, but
     * essentially a call to DatabaseMetaData.getColumns().
     *
     * @param md
     * @param tableName
     * @return
     * @throws SQLException
     */
    protected ResultSet getColumnResultSet(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getColumns(null, config.getSchema(), tableName, null);
    }

    /**
     * For a DatabaseMetaData.getColumns ResultSet, calls
     * rs.getString("COLUMN_NAME") to return the name of the column.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private String getColumnName(final ResultSet rs) throws SQLException {
        return rs.getString("COLUMN_NAME");
    }

    /**
     * For a DatabaseMetaData.getColumns ResultSet, calls 
     * rs.getInt("COLUMN_SIZE") to return the size of the column.
     *
     * @param rs
     * @return
     * @throws SQLException 
     */
    private int getColumnSize(final ResultSet rs) throws SQLException {
        return rs.getInt("COLUMN_SIZE");
    }

    /**
     * For a DatabaseMetaData.getColumns ResultSet, calls
     * rs.getString("DATA_TYPE") and uses SqlTypeToClass.getTypeFrom() to get a
     * Class to represent the type of column.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private Class getColumnType(final ResultSet rs) throws SQLException {
        int type = rs.getInt("DATA_TYPE");
        return sqlTypeMap.getTypeFrom(type);
    }

    /**
     * Returns a Map of column names as keys, and values representing the
     * foreign key relationship for the passed DatabaseMetaData and tableName.
     *
     * @param md
     * @param tableName
     * @return
     * @throws SQLException
     */
    private Map<String, String> getForeignKeysMap(final DatabaseMetaData md, final String tableName) throws SQLException {
        Map<String, String> ret = new HashMap<>();
        try (ResultSet rs = getForeignKeysResultSet(md, tableName)) {
            while (rs.next()) {
                String col = rs.getString("FKCOLUMN_NAME").toLowerCase(Locale.ENGLISH);
                String fkey = (rs.getString("PKTABLE_NAME") + "." + rs.getString("PKCOLUMN_NAME")).toLowerCase(Locale.ENGLISH);
                log.debug("Found foreign key for column: {} referencing: {}", col, fkey);
                ret.put(col, fkey);
            }
        }
        return ret;
    }

    /**
     * Returns a ResultSet for foreign keys of the passed DatabaseMetaData and
     * tableName.
     *
     * @param md
     * @param tableName
     * @return
     * @throws SQLException
     */
    protected ResultSet getForeignKeysResultSet(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getImportedKeys(null, config.getSchema(), tableName);
    }

    /**
     * Returns a List of column names representing primary keys for the passed
     * DatabaseMetaData and tableName.
     *
     * @param md
     * @param tableName
     * @return
     * @throws SQLException
     */
    private List<String> getPrimaryKeysList(final DatabaseMetaData md, final String tableName) throws SQLException {
        List<String> ret = new ArrayList<>();
        try (ResultSet rs = getPrimaryKeysResultSet(md, tableName)) {
            while (rs.next()) {
                String pkey = rs.getString("COLUMN_NAME");
                log.debug("Found primary key: {}", pkey);
                ret.add(pkey.toLowerCase(Locale.ENGLISH));
            }
        }
        return ret;
    }

    /**
     * Returns a ResultSet for primary keys of the passed DatabaseMetaData and
     * tableName.
     *
     * @param md
     * @param tableName
     * @return
     * @throws SQLException
     */
    protected ResultSet getPrimaryKeysResultSet(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getPrimaryKeys(null, config.getSchema(), tableName);
    }

    /**
     * Finds and adds columns to the passed TableMetaData.
     * @param md
     * @param table
     * @throws SQLException
     */
    private void addColumnMetaData(DatabaseMetaData md, TableMetaData table) throws SQLException {

        final List<String> pKeys = getPrimaryKeysList(md, table.getTableName());
        final Map<String, String> fKeys = getForeignKeysMap(md, table.getTableName());

        try (ResultSet crs = getColumnResultSet(md, table.getTableName())) {
            while (crs.next()) {
                String columnName = getColumnName(crs);
                boolean isPrimaryKey = pKeys.contains(columnName.toLowerCase(Locale.ENGLISH));
                String foreignKey = fKeys.get(columnName.toLowerCase(Locale.ENGLISH));
                log.debug("Found metadata for column {} in table {}", columnName, table);
                table.addColumn(columnName, getColumnType(crs), getColumnSize(crs), isPrimaryKey, foreignKey);
            }
        }
    }
}
