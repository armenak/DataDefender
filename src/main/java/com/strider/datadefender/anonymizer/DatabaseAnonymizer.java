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
package com.strider.datadefender.anonymizer;

import com.strider.datadefender.requirement.functions.DatabaseAwareRequirementFunctionClass;
import com.strider.datadefender.requirement.functions.RequirementFunctionClassRegistry;
import com.strider.datadefender.requirement.functions.RequirementFunctionClass;
import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.DbConfig;
import com.strider.datadefender.database.DatabaseException;
import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.database.metadata.TableMetaData;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.functions.CoreFunctions;
import com.strider.datadefender.functions.Utils;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.Exclude;
import com.strider.datadefender.requirement.Key;
import com.strider.datadefender.requirement.Parameter;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.Table;
import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.utils.LikeMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;

/**
 * Entry point for RDBMS data anonymizer
 *
 * @author Armenak Grigoryan
 */
@RequiredArgsConstructor
@Log4j2
public class DatabaseAnonymizer implements IAnonymizer {

    final IDbFactory dbFactory;
    final DbConfig config;
    final int batchSize;
    final Requirement requirement;
    final List<String> tables;

    /**
     * Adds column names from the table to the passed collection of strings.
     *
     * @param table
     * @param sColumns
     */
    private void fillColumnNames(final Table table, final Collection<String> sColumns) {
        for (final Column column : table.getColumns()) {
            sColumns.add(column.getName());
        }
    }

    /**
     * Adds column names that make up the table's primary key.
     *
     * @param table
     * @return
     */
    private void fillPrimaryKeyNamesList(final Table table, final Collection<String> sKeys) {
        final List<Key> pKeys = table.getPrimaryKeys();
        if (pKeys != null && pKeys.size() != 0) {
            for (final Key key : pKeys) {
                sKeys.add(key.getName());
            }
        } else {
            sKeys.add(table.getPkey());
        }
    }

    /**
     * Creates the UPDATE query for a single row of results.
     *
     * @param table
     * @param columns
     * @param keys
     * @param updatableKeys
     * @return the SQL statement
     */
    private String getUpdateQuery(final Table table, final Collection<String> updateColumns, final Collection<String> keys) {
        final StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").
            append(table.getName()).
            append(" SET ").
            append(StringUtils.join(updateColumns, " = ?, ")).
            append(" = ? WHERE ");

        log.info("keys: " + keys.toString());
        int iteration = 0;
        final int collectionSize = keys.size();
        final StringBuilder whereStmtp = new StringBuilder();
        for (final String key: keys) {
            ++iteration;
            whereStmtp.append(key).append(" = ? ");
            if (collectionSize > iteration) {
                whereStmtp.append(" AND ");
            }
        }
        sql.append(whereStmtp);

        log.debug("getUpdateQuery: " + sql.toString());
        return sql.toString();
    }

    /**
     * Creates the SELECT query for key and update columns.
     *
     * @param tableName
     * @param keys
     * @param columns
     * @return
     */
    private PreparedStatement getSelectQueryStatement(final IDbFactory dbFactory, final Table table, final Collection<String> keys, final Collection<String> columns) throws SQLException {

        final List<String> params = new LinkedList<>();
        // final StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        final StringBuilder query = new StringBuilder("SELECT ");
        query.append(StringUtils.join(keys, ", ")).
              append(", ").
              append(StringUtils.join(columns, ", ")).
              append(" FROM ").
              append(table.getName());

        if (!StringUtils.isBlank(table.getWhere())) {
            query.append(" WHERE (").append(table.getWhere());
        }

        final List<Exclude> exclusions = table.getExclusions();
        if (exclusions != null) {
            String separator = " WHERE (";
            if (query.indexOf(" WHERE (") != -1) {
                separator = ") AND (";
            }
            for (final Exclude exc : exclusions) {
                final String eq = exc.getEquals();
                final String lk = exc.getLike();
                final List<String> in = exc.getExcludeInList();
                final boolean nl = exc.isExcludeNull();
                final String col = exc.getName();

                if (col != null && col.length() != 0) {
                    if (eq != null) {
                        query.append(separator).append('(').append(col).append(" != ? OR ").append(col).append(" IS NULL)");
                        params.add(eq);
                        separator = " AND ";
                    }
                    if (lk != null && lk.length() != 0) {
                        query.append(separator).append('(').append(col).append(" NOT LIKE ? OR ").append(col).append(" IS NULL)");
                        params.add(lk);
                        separator = " AND ";
                    }
                    if (CollectionUtils.isNotEmpty(in)) {
                        String qs = "?" + StringUtils.repeat(", ?", in.size() - 1);
                        query.append(separator).append('(').append(col).append(" NOT IN (")
                            .append(qs).append(") OR ").append(col).append(" IS NULL)");
                        params.addAll(in);
                        separator = " AND ";
                    }
                    if (nl) {
                        query.append(separator).append(col).append(" IS NOT NULL");
                        separator = " AND ";
                    }
                }
            }

            if (query.indexOf(" WHERE (") != -1) {
                separator = ") AND (";
            }

            for (final Exclude exc : exclusions) {
                final String neq = exc.getNotEquals();
                final String nlk = exc.getNotLike();
                final List<String> nin = exc.getExcludeNotInList();
                final String col = exc.getName();

                if (neq != null) {
                    query.append(separator).append(col).append(" = ?");
                    separator = " OR ";
                }
                if (nlk != null && nlk.length() != 0) {
                    query.append(separator).append(col).append(" LIKE ?");
                    separator = " OR ";
                }
                if (CollectionUtils.isNotEmpty(nin)) {
                    String qs = "?" + StringUtils.repeat(", ?", nin.size() - 1);
                    query.append(separator).append(col).append(" IN (").append(qs).append(")");
                    params.addAll(nin);
                    separator = " OR ";
                }
            }
        }
        if (query.indexOf(" WHERE (") != -1) {
            query.append(')');
        }

        final PreparedStatement stmt = dbFactory.getConnection().prepareStatement(
                query.toString(),
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_UPDATABLE
        );
        if (dbFactory.getVendorName().equalsIgnoreCase("mysql")) {
            stmt.setFetchSize(Integer.MIN_VALUE);
        }

        int paramIndex = 1;
        for (final String param : params) {
            stmt.setString(paramIndex, param);
            ++paramIndex;
        }

        log.debug("Querying for: " + query.toString());
        if (params.size() > 0) {
            log.debug("\t - with parameters: " + StringUtils.join(params, ','));
        }

        return stmt;
    }

    /**
     * Returns true if the current column's value is excluded by the rulesets
     * defined by the Requirements.
     *
     * @param db
     * @param row
     * @param column
     * @return the columns value
     * @throws SQLException
     */
    private boolean isExcludedColumn(final ResultSet row, final Column column) throws SQLException {

        final String columnName = column.getName();

        final List<Exclude> exclusions = column.getExclusions();
        boolean hasInclusions = false;
        boolean passedInclusion = false;

        if (exclusions != null) {
            for (final Exclude exc : exclusions) {
                String name = exc.getName();
                final String eq = exc.getEquals();
                final String lk = exc.getLike();
                final String neq = exc.getNotEquals();
                final String nlk = exc.getNotLike();
                final boolean nl = exc.isExcludeNull();
                if (name == null || name.length() == 0) {
                    name = columnName;
                }
                final String testValue = row.getString(name);

                if (nl && testValue == null) {
                    return true;
                } else if (eq != null && eq.equals(testValue)) {
                    return true;
                } else if (lk != null && lk.length() != 0) {
                    final LikeMatcher matcher = new LikeMatcher(lk);
                    if (matcher.matches(testValue)) {
                        return true;
                    }
                }

                if (neq != null) {
                    hasInclusions = true;
                    if (neq.equals(testValue)) {
                        passedInclusion = true;
                    }
                }
                if (nlk != null && nlk.length() != 0) {
                    hasInclusions = true;
                    final LikeMatcher matcher = new LikeMatcher(nlk);
                    if (matcher.matches(testValue)) {
                        passedInclusion = true;
                    }
                }
            }
        }

        return hasInclusions && !passedInclusion;
    }

    /**
     * Returns the passed colValue truncated to the column's size in the table.
     *
     * @param colValue
     * @param colName
     * @param columnMetaData
     * @return
     * @throws SQLException
     */
    private String getTruncatedColumnValue(final String colValue, final String colName, final TableMetaData tableMetaData) throws SQLException {
        final ColumnMetaData col = tableMetaData.getColumn(colName);
        final int colSize = col.getColumnSize();
        final Class clazz = col.getColumnType();
        if (clazz.equals(String.class) && colValue.length() > colSize) {
            return colValue.substring(0, colSize);
        }
        return colValue;
    }

    /**
     * Anonymizes a row of columns.
     *
     * Sets query parameters on the passed updateStmt - this includes the key
     * values - and calls anonymization functions for the columns.
     *
     * @param updateStmt
     * @param tableColumns
     * @param keyNames
     * @param db
     * @param row
     * @param columnMetaData
     * @throws SQLException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private void anonymizeRow(
        final PreparedStatement updateStmt,
        final Collection<Column> tableColumns,
        final Collection<String> keyNames,
        final ResultSet row,
        final TableMetaData tableMetaData
    ) throws SQLException,
             NoSuchMethodException,
             SecurityException,
             IllegalAccessException,
             IllegalArgumentException,
             InvocationTargetException,
             DatabaseException {

        int fieldIndex = 0;
        final Map<String, Integer> columnIndexes = new HashMap<>(tableColumns.size());
        final Set<String> anonymized = new HashSet<>(tableColumns.size());

        for (final Column column : tableColumns) {
            final String columnName = column.getName();
            if (anonymized.contains(columnName)) {
                continue;
            }
            if (!columnIndexes.containsKey(columnName)) {
                final int columnIndex = ++fieldIndex;
                columnIndexes.put(columnName, columnIndex);
            }
            if (isExcludedColumn(row, column)) {
                final String columnValue = row.getString(columnName);
                updateStmt.setString(columnIndexes.get(columnName), columnValue);
                log.debug("Excluding column: " + columnName + " with value: " + columnValue);
                continue;
            }

            anonymized.add(columnName);
            final Object colValue = column.invokeFunction(row);
            log.debug("colValue = " + colValue);
            log.debug("type= " + (colValue != null ? colValue.getClass() : "null"));
            if (colValue == null) {
                updateStmt.setNull(columnIndexes.get(columnName), Types.NULL);
            } else if (colValue.getClass() == java.sql.Date.class) {
                updateStmt.setDate(columnIndexes.get(columnName), CommonUtils.stringToDate(colValue.toString(), "dd-MM-yyyy") );
            } else if (colValue.getClass() == java.lang.Integer.class) {
                updateStmt.setInt(columnIndexes.get(columnName), (int) colValue);
            } else {
                updateStmt.setString(
                    columnIndexes.get(columnName),
                    getTruncatedColumnValue(
                        (String) colValue,
                        columnName,
                        tableMetaData
                    )
                );
            }
        }

        int whereIndex = fieldIndex;
        for (final String key : keyNames) {
            updateStmt.setString(++whereIndex, row.getString(key));
        }

        updateStmt.addBatch();
    }

    /**
     * Anonymization function for a single table.
     *
     * Sets up queries, loops over columns and anonymizes columns for the passed
     * Table.
     *
     * @param table
     */
    private void anonymizeTable(final Table table) throws DatabaseException {

        if (StringUtils.isBlank(table.getWhere())) {
            log.info("Table [" + table.getName() + "]. Start ...");
        } else {
            log.info("Table [" + table.getName() + ", where=" + table.getWhere() + "]. Start ...");
        }

        final List<Column> tableColumns = table.getColumns();
        // colNames is looked up with contains, and iterated over.  Using LinkedHashSet means
        // duplicate column names won't be added to the query, so a check in the column loop
        // below was created to ensure a reasonable warning message is logged if that happens.
        final Set<String> colNames = new LinkedHashSet<>(tableColumns.size());
        // keyNames is only iterated over, so no need for a hash set
        final List<String> keyNames = new LinkedList<>();

        fillColumnNames(table, colNames);
        fillPrimaryKeyNamesList(table, keyNames);

        // required in this scope for 'catch' block
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        final Connection updateCon = dbFactory.getUpdateConnection();

        try {
            selectStmt = getSelectQueryStatement(dbFactory, table, keyNames, colNames);
            rs = selectStmt.executeQuery();

            final TableMetaData tableMetaData = dbFactory.fetchMetaData().getMetaDataFor(rs);

            final String updateString = getUpdateQuery(table, colNames, keyNames);
            updateStmt = updateCon.prepareStatement(updateString);

            int batchCounter = 0;
            int rowCount = 0;

            while (rs.next()) {
                anonymizeRow(updateStmt, tableColumns, keyNames, rs, tableMetaData);
                batchCounter++;
                if (batchCounter == batchSize) {
                    updateStmt.executeBatch();
                    updateCon.commit();
                    batchCounter = 0;
                }
                rowCount++;
            }
            log.debug("Rows processed: " + rowCount);

            updateStmt.executeBatch();
            log.debug("Batch executed");
            updateCon.commit();
            log.debug("Commit");
            selectStmt.close();
            updateStmt.close();
            rs.close();
            log.debug("Closing open resources");

        } catch (SQLException | NoSuchMethodException | SecurityException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException | DataDefenderException ex ) {
            log.error(ex.toString());
            if (ex.getCause() != null) {
                log.error(ex.getCause().toString());
            }
            try {
                if (selectStmt != null) {
                    selectStmt.close();
                }
                if (updateStmt != null) {
                    updateStmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException sqlex) {
                log.error(sqlex.toString());
            }
        } finally {
            try {
                if (selectStmt != null) {
                    selectStmt.close();
                }
                if (updateStmt != null) {
                    updateStmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException sqlex) {
                log.error(sqlex.toString());
            }
        }

        log.info("Table " + table.getName() + ". End ...");
        log.info("");
    }

    public void anonymize() throws DataDefenderException {
        log.info("Anonymizing data for client " + requirement.getClient() + " Version " + requirement.getVersion());
        for (final Table reqTable : requirement.getFilteredTables(tables)) {
            anonymizeTable(reqTable);
        }
    }
}
