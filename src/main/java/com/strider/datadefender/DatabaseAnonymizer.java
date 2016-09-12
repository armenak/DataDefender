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

package com.strider.datadefender;

import static org.apache.log4j.Logger.getLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.strider.datadefender.database.DatabaseAnonymizerException;
import com.strider.datadefender.database.IDBFactory;
import com.strider.datadefender.database.metadata.MatchMetaData;
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
import com.strider.datadefender.utils.RequirementUtils;

/**
 * Entry point for RDBMS data anonymizer
 * 
 * @author Armenak Grigoryan
 */
public class DatabaseAnonymizer implements IAnonymizer { 
    
    private static final Logger log = getLogger(DatabaseAnonymizer.class);

    
    /**
     * Adds column names from the table to the passed collection of strings.
     * 
     * @param table
     * @param sColumns 
     */
    private void fillColumnNames(final Table table, Collection<String> sColumns) {
        for (Column column : table.getColumns()) {
            sColumns.add(column.getName());
        }
    }
    
    /**
     * Adds column names that make up the table's primary key.
     * 
     * @param table
     * @return 
     */
    private void fillPrimaryKeyNamesList(final Table table, Collection<String> sKeys) {
        final List<Key> pKeys = table.getPrimaryKeys();
        if (pKeys != null && pKeys.size() != 0) {
            for (Key key : pKeys) {
                sKeys.add(key.getName());
            }
        } else {
            sKeys.add(table.getPKey());
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
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(table.getName()).append(" SET ");
        sql.append(StringUtils.join(updateColumns, " = ?, ")).append(" = ?");
        sql.append(" WHERE " ).append(StringUtils.join(keys, " = ? AND ")).append(" = ?");
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
    private PreparedStatement getSelectQueryStatement(final IDBFactory dbFactory, final Table table, final Collection<String> keys, final Collection<String> columns) throws SQLException {
        
        final List<String> params = new LinkedList<>();
        final StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        query.append(StringUtils.join(keys, ", ")).append(", ");
        query.append(StringUtils.join(columns, ", "));
        query.append(" FROM ").append(table.getName());
        
        List<Exclude> exclusions = table.getExclusions();
        if (exclusions != null) {
            String separator = " WHERE (";
            for (final Exclude exc : exclusions) {
                final String eq = exc.getEqualsValue();
                final String lk = exc.getLikeValue();
                final boolean nl = exc.isExcludeNulls();
                final String col = exc.getName();

                if (col != null && col.length() != 0) {
                    if (eq != null) {
                        query.append(separator).append("(").append(col).append(" != ? OR ").append(col).append(" IS NULL)");
                        params.add(eq);
                        separator = " AND ";
                    }
                    if (lk != null && lk.length() != 0) {
                        query.append(separator).append("(").append(col).append(" NOT LIKE ? OR ").append(col).append(" IS NULL)");
                        params.add(lk);
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
                final String neq = exc.getNotEqualsValue();
                final String nlk = exc.getNotLikeValue();
                final String col = exc.getName();
                
                if (neq != null) {
                    query.append(separator).append(col).append(" = ?");
                    separator = " OR ";
                }
                if (nlk != null && nlk.length() != 0) {
                    query.append(separator).append(col).append(" LIKE ?");
                    separator = " OR ";
                }

            }
            
            if (query.indexOf(" WHERE (") != -1) {
                query.append(")");
            }
        }
        
        final PreparedStatement stmt = dbFactory.getConnection().prepareStatement(query.toString());
        if (dbFactory.getVendorName().equalsIgnoreCase("mysql")) {
            stmt.setFetchSize(Integer.MIN_VALUE);
        }
        
        int paramIndex = 1;
        for (String param : params) {
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
     * Calls the anonymization function for the given Column, and returns its
     * anonymized value.
     * 
     * @param dbConn
     * @param row
     * @param column
     * @return anonymized value
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */   
    private Object callAnonymizingFunctionFor(final Connection dbConn, final ResultSet row, final Column column)
        throws SQLException,
               NoSuchMethodException,
               SecurityException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException {
        
        // Check if function has parameters
        final List<Parameter> parms = column.getParameters();
        if (parms != null) {
            return callAnonymizingFunctionWithParameters(dbConn, row, column);
        } else {
            return callAnonymizingFunctionWithoutParameters(dbConn, column);
        }
        
    }    
    
    /**
     * Calls the anonymization function for the given Column, and returns its
     * anonymized value.
     * 
     * @param dbConn
     * @param row
     * @param column
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    private Object callAnonymizingFunctionWithParameters(final Connection dbConn, final ResultSet row, final Column column)
        throws SQLException,
               NoSuchMethodException,
               SecurityException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException {
        
        final String columnValue = row.getString(column.getName());
        final String function = column.getFunction();
        if (function == null || function.equals("")) {
            log.warn("Function is not defined for column [" + column + "]. Moving to the next column.");
            return "";
        } 
        
        try {
            final String className = Utils.getClassName(function);
            final String methodName = Utils.getMethodName(function);
            final Class<?> clazz = Class.forName(className);
            
            final CoreFunctions instance = (CoreFunctions) Class.forName(className).newInstance();
            instance.setDatabaseConnection(dbConn);

            final List<Parameter> parms = column.getParameters();
            Map<String, Object> paramValues = new HashMap<>(parms.size());
            for (Parameter param : parms) {
                if (param.getValue().equals("@@value@@")) {
                    paramValues.put(param.getName(), columnValue);
                } else if (param.getValue().equals("@@row@@") && param.getType().equals("java.sql.ResultSet")) {
                    paramValues.put(param.getName(), row);
                } else {
                    paramValues.put(param.getName(), param.getTypeValue());
                }
            }
            
            List<Object> fnArguments = new ArrayList<>(parms.size());
            final Method[] methods = clazz.getMethods();
            Method selectedMethod = null;
            
            methodLoop:
            for (final Method m : methods) {
                if (m.getName().equals(methodName) && m.getReturnType() == String.class) {
                    
                    log.debug("  Found method: " + m.getName());
                    log.debug("  Match w/: " + paramValues);
                    
                    final java.lang.reflect.Parameter[] mParams = m.getParameters();
                    fnArguments.clear();
                    for (final java.lang.reflect.Parameter par : mParams) {
                        //log.debug("    Name present? " + par.isNamePresent());
                        // Note: requires -parameter compiler flag
                        log.debug("    Real param: " + par.getName());
                        if (!paramValues.containsKey(par.getName())) {
                            continue methodLoop;
                        }
                        
                        final Object value = paramValues.get(par.getName());
                        Class<?> fnParamType = par.getType();
                        final Class<?> confParamType = (value == null) ? fnParamType : value.getClass();
                        
                        if (fnParamType.isPrimitive() && value == null) {
                            continue methodLoop;
                        }
                        if (ClassUtils.isPrimitiveWrapper(confParamType)) {
                            if (!ClassUtils.isPrimitiveOrWrapper(fnParamType)) {
                                continue methodLoop;
                            }
                            fnParamType = ClassUtils.primitiveToWrapper(fnParamType);
                        }
                        if (fnParamType != confParamType) {
                            continue methodLoop;
                        }
                        fnArguments.add(value);
                    }
                    
                    // actual parameters check less than xml defined parameters size, because values could be auto-assigned (like 'values' and 'row' params)
                    if (fnArguments.size() != mParams.length || fnArguments.size() < paramValues.size()) {
                        continue;
                    }
                    
                    selectedMethod = m;
                    break;
                }
            }
            
            if (selectedMethod == null) {
                StringBuilder s = new StringBuilder("Anonymization method: ");
                s.append(methodName).append(" with parameters matching (");
                String comma = "";
                for (final Parameter p : parms) {
                    s.append(comma).append(p.getType()).append(" ").append(p.getName());
                    comma = ", ";
                }
                s.append(") was not found in class ").append(className);
                throw new NoSuchMethodException(s.toString());
            }
            
            log.debug("Anonymizing function: " + methodName + " with parameters: " + Arrays.toString(fnArguments.toArray()));
            final Object anonymizedValue = selectedMethod.invoke(instance, fnArguments.toArray());
            if (anonymizedValue == null) {
                return null;
            }
            return anonymizedValue.toString();
            
        } catch (AnonymizerException | InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }
        
        return "";
    }
    
    /**
     * Calls the anonymization function for the given Column, and returns its
     * anonymized value.
     * 
     * @param dbConn
     * @param row
     * @param column
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    private Object callAnonymizingFunctionWithoutParameters(final Connection dbConn, final Column column)
    throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
           InvocationTargetException {
        
        String function = column.getFunction();
        if (function == null || function.equals("")) {
            log.warn("Function is not defined for column [" + column + "]. Moving to the next column.");
            return "";
        } 
        
        try {
            final String className = Utils.getClassName(function);
            final String methodName = Utils.getMethodName(function);
            final Class<?> clazz = Class.forName(className);
            
            final CoreFunctions instance = (CoreFunctions) Class.forName(className).newInstance();
            instance.setDatabaseConnection(dbConn);

            Method[] methods = clazz.getMethods();
            Method selectedMethod = null;
            Object returnType = null;
            
            methodLoop:
            for (final Method m : methods) {
                //if (m.getName().equals(methodName) && m.getReturnType() == String.class) {
                if (m.getName().equals(methodName)) {
                    log.debug("  Found method: " + m.getName());
                    selectedMethod = m;
                    returnType = m.getReturnType();
                    break;
                }
            }
            
            if (selectedMethod == null) {
                final StringBuilder s = new StringBuilder("Anonymization method: ");
                s.append(methodName).append(") was not found in class ").append(className);
                throw new NoSuchMethodException(s.toString());
            }
            
            log.debug("Anonymizing function: " + methodName);
            final Object anonymizedValue = selectedMethod.invoke(instance);
            if (anonymizedValue == null) {
                return null;
            }
            
            if (returnType == String.class) {
                return anonymizedValue.toString();
            } else if (returnType == java.sql.Date.class) {
                return anonymizedValue;
            }
        } catch (AnonymizerException | InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }
        
        return "";
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
    private boolean isExcludedColumn(ResultSet row, Column column) throws SQLException {
        
        String columnName = column.getName();
        
        List<Exclude> exclusions = column.getExclusions();
        boolean hasInclusions = false;
        boolean passedInclusion = false;
        
        if (exclusions != null) {
            for (final Exclude exc : exclusions) {
                String name = exc.getName();
                String eq = exc.getEqualsValue();
                String lk = exc.getLikeValue();
                String neq = exc.getNotEqualsValue();
                final String nlk = exc.getNotLikeValue();
                boolean nl = exc.isExcludeNulls();
                if (name == null || name.length() == 0) {
                    name = columnName;
                }
                String testValue = row.getString(name);

                if (nl && testValue == null) {
                    return true;
                } else if (eq != null && eq.equals(testValue)) {
                    return true;
                } else if (lk != null && lk.length() != 0) {
                    LikeMatcher matcher = new LikeMatcher(lk);
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
                    LikeMatcher matcher = new LikeMatcher(nlk);
                    if (matcher.matches(testValue)) {
                        passedInclusion = true;
                    }
                }
            }
        }

        return (hasInclusions && !passedInclusion);
    }
    
    /**
     * Returns the passed colValue truncated to the column's size in the table.
     * 
     * @param colValue
     * @param index
     * @param columnMetaData
     * @return
     * @throws SQLException 
     */
    private String getTruncatedColumnValue(String colValue, int index, List<MatchMetaData> columnMetaData) throws SQLException {
        MatchMetaData md = columnMetaData.get(index);
        int colSize = md.getColumnSize();
        String type = md.getColumnType();
        if ("String".equals(type) && colValue.length() > colSize) {
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
        PreparedStatement updateStmt,
        Collection<Column> tableColumns,
        Collection<String> keyNames,
        Connection db,
        ResultSet row,
        List<MatchMetaData> columnMetaData
    ) throws SQLException,
             NoSuchMethodException,
             SecurityException,
             IllegalAccessException,
             IllegalArgumentException,
             InvocationTargetException,
             AnonymizerException {
        
        int fieldIndex = 0;
        Map<String, Integer> columnIndexes = new HashMap<>(tableColumns.size());
        Set<String> anonymized = new HashSet<>(tableColumns.size());

        for (Column column : tableColumns) {
            String columnName = column.getName();
            if (anonymized.contains(columnName)) {
                continue;
            }
            if (!columnIndexes.containsKey(columnName)) {
                int columnIndex = ++fieldIndex;
                columnIndexes.put(columnName, columnIndex);
            }
            if (isExcludedColumn(row, column)) {
                String columnValue = row.getString(columnName);
                updateStmt.setString(columnIndexes.get(columnName), columnValue);
                log.debug("Excluding column: " + columnName + " with value: " + columnValue);
                continue;
            }
            
            anonymized.add(columnName);
            Object colValue = callAnonymizingFunctionFor(db, row, column);
            if (colValue == null) {
                updateStmt.setNull(columnIndexes.get(columnName), Types.NULL);
            } else if (colValue.getClass() == java.sql.Date.class) {
                updateStmt.setDate(columnIndexes.get(columnName), CommonUtils.stringToDate(colValue.toString(), "dd-MM-yyyy") );
            } else {
                updateStmt.setString(
                    columnIndexes.get(columnName),
                    getTruncatedColumnValue(
                        (String) colValue,
                        columnIndexes.get(columnName),
                        columnMetaData
                    )
                );
            }
        }

        int whereIndex = fieldIndex;
        for (String key : keyNames) {
            String value = row.getString(key);
            updateStmt.setString(++whereIndex, value);
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
    private void anonymizeTable(int batchSize, IDBFactory dbFactory, Table table) 
    throws AnonymizerException {
        
        log.info("Table [" + table.getName() + "]. Start ...");
        
        List<Column> tableColumns = table.getColumns();
        // colNames is looked up with contains, and iterated over.  Using LinkedHashSet means
        // duplicate column names won't be added to the query, so a check in the column loop
        // below was created to ensure a reasonable warning message is logged if that happens.
        Set<String> colNames = new LinkedHashSet<>(tableColumns.size());
        // keyNames is only iterated over, so no need for a hash set
        List<String> keyNames = new LinkedList<>();
        
        fillColumnNames(table, colNames);
        fillPrimaryKeyNamesList(table, keyNames);
        
        // required in this scope for 'catch' block
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        Connection updateCon = dbFactory.getUpdateConnection();
        
        try {

            selectStmt = getSelectQueryStatement(dbFactory, table, keyNames, colNames);
            rs = selectStmt.executeQuery();
            
            List<MatchMetaData> columnMetaData = dbFactory.fetchMetaData().getMetaDataForRs(rs);
            
            String updateString = getUpdateQuery(table, colNames, keyNames);
            updateStmt = updateCon.prepareStatement(updateString);
            log.debug("Update SQL: " + updateString);
            
            int batchCounter = 0; 
            int rowCount = 0;
            
            while (rs.next()) {
                anonymizeRow(updateStmt, tableColumns, keyNames, updateCon, rs, columnMetaData);
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
            updateCon.commit();
            selectStmt.close();
            updateStmt.close();
            rs.close();
            
        } catch (SQLException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
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
        }
        
        log.info("Table " + table.getName() + ". End ...");
        log.info("");
    }
    
    @Override
    public void anonymize(IDBFactory dbFactory, Properties anonymizerProperties, Set<String> tables) 
    throws DatabaseAnonymizerException, AnonymizerException{

        int batchSize = Integer.parseInt(anonymizerProperties.getProperty("batch_size"));
        Requirement requirement = RequirementUtils.load(anonymizerProperties.getProperty("requirement"));
        
        // Iterate over the requirement
        log.info("Anonymizing data for client " + requirement.getClient() + " Version " + requirement.getVersion());
        for(Table reqTable : requirement.getTables()) {
            if (tables.isEmpty() || tables.contains(reqTable.getName())) {
                anonymizeTable(batchSize, dbFactory, reqTable);
            }
        }
    }
}
