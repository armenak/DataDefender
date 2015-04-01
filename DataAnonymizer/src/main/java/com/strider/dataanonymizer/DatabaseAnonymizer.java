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

import static javax.xml.bind.JAXBContext.newInstance;
import static org.apache.log4j.Logger.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.database.IDBConnection;
import com.strider.dataanonymizer.database.IDBFactory;
import com.strider.dataanonymizer.functions.CoreFunctions;
import com.strider.dataanonymizer.functions.Utils;
import com.strider.dataanonymizer.requirement.Column;
import com.strider.dataanonymizer.requirement.Exclude;
import com.strider.dataanonymizer.requirement.Key;
import com.strider.dataanonymizer.requirement.Parameter;
import com.strider.dataanonymizer.requirement.Requirement;
import com.strider.dataanonymizer.requirement.Table;
import com.strider.dataanonymizer.utils.LikeMatcher;

/**
 * Entry point for RDBMS data anonymizer
 * 
 * @author Armenak Grigoryan
 */
public class DatabaseAnonymizer implements IAnonymizer { 
    
    private static Logger log = getLogger(DatabaseAnonymizer.class);

    /**
     * Returns a Requirement object for a given XML file.
     * 
     * Initializes JAXB parser and parses, returning the parsed Requirement
     * object
     * 
     * @param file the filename
     * @return
     * @throws DatabaseAnonymizerException 
     */
    private Requirement getRequirement(String file) throws DatabaseAnonymizerException {
        Requirement req = null;
        try {
            JAXBContext jc = newInstance(Requirement.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            req = (Requirement) unmarshaller.unmarshal(new FileInputStream(new File(file)));
        } catch (JAXBException je) {
            log.error(je.toString());
            throw new DatabaseAnonymizerException(je.toString(), je);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(DatabaseAnonymizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return req;
    }
    
    /**
     * Adds column names from the table to the passed collection of strings.
     * 
     * @param table
     * @param sColumns 
     */
    private void fillColumnNames(Table table, Collection<String> sColumns) {
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
    private void fillPrimaryKeyNamesList(Table table, Collection<String> sKeys) {
        List<Key> pKeys = table.getPrimaryKeys();
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
    private String getUpdateQuery(Table table, Collection<String> updateColumns, Collection<String> keys) {
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
    private PreparedStatement getSelectQueryStatement(Connection db, Table table, Collection<String> keys, Collection<String> columns) throws SQLException {
        
        List<String> params = new LinkedList<>();
        StringBuilder query = new StringBuilder("SELECT DISTINCT ");
        query.append(StringUtils.join(keys, ", ")).append(", ");
        query.append(StringUtils.join(columns, ", "));
        query.append(" FROM ").append(table.getName());
        
        List<Exclude> exclusions = table.getExclusions();
        if (exclusions != null) {
            String separator = " WHERE (";
            for (Exclude exc : exclusions) {
                String eq = exc.getEqualsValue();
                String lk = exc.getLikeValue();
                boolean nl = exc.isExcludeNulls();
                String col = exc.getName();

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
            
            for (Exclude exc : exclusions) {
                String neq = exc.getNotEqualsValue();
                String nlk = exc.getNotLikeValue();
                String col = exc.getName();
                
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
        
        PreparedStatement stmt = db.prepareStatement(query.toString());
        int paramIndex = 1;
        for (String param : params) {
            stmt.setString(paramIndex, param);
            ++paramIndex;
        }
        
        log.debug("Querying for: " + query.toString());
        log.debug("\t - with parameters: " + StringUtils.join(params, ','));
        
        return stmt;
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
    private String callAnonymizingFunctionFor(Connection dbConn, ResultSet row, Column column)
        throws SQLException,
               NoSuchMethodException,
               SecurityException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException {
        
        String columnValue = row.getString(column.getName());
        String function = column.getFunction();
        if (function == null || function.equals("")) {
            log.warn("Function is not defined for column [" + column + "]. Moving to the next column.");
            return "";
        } 
        
        try {
            
            String className = Utils.getClassName(function);
            String methodName = Utils.getMethodName(function);
            Class<?> clazz = Class.forName(className);
            
            CoreFunctions instance = (CoreFunctions) Class.forName(className).newInstance();
            instance.setDatabaseConnection(dbConn);

            List<Parameter> parms = column.getParameters();
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
            Method[] methods = clazz.getMethods();
            Method selectedMethod = null;
            
            methodLoop:
            for (Method m : methods) {
                if (m.getName().equals(methodName) && m.getReturnType() == String.class) {
                    java.lang.reflect.Parameter[] mParams = m.getParameters();
                    fnArguments.clear();
                    for (java.lang.reflect.Parameter par : mParams) {
                        
                        if (!paramValues.containsKey(par.getName())) {
                            continue methodLoop;
                        }
                        
                        Object value = paramValues.get(par.getName());
                        Class<?> fnParamType = par.getType();
                        Class<?> confParamType = (value == null) ? fnParamType : value.getClass();
                        
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
                for (Parameter p : parms) {
                    s.append(comma).append(p.getType()).append(" ").append(p.getName());
                    comma = ", ";
                }
                s.append(") was not found in class ").append(className);
                throw new NoSuchMethodException(s.toString());
            }
            
            log.debug("Anonymizing function: " + methodName + " with parameters: " + Arrays.toString(fnArguments.toArray()));
            Object anonymizedValue = selectedMethod.invoke(instance, fnArguments.toArray());
            if (anonymizedValue == null) {
                return null;
            }
            return anonymizedValue.toString();
            
        } catch (InstantiationException | ClassNotFoundException ex) {
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
    private boolean isExcludedColumn(Connection db, ResultSet row, Column column) throws SQLException {
        
        String columnName = column.getName();
        
        List<Exclude> exclusions = column.getExclusions();
        boolean hasInclusions = false;
        boolean passedInclusion = false;
        
        if (exclusions != null) {
            for (Exclude exc : exclusions) {
                String name = exc.getName();
                String eq = exc.getEqualsValue();
                String lk = exc.getLikeValue();
                String neq = exc.getNotEqualsValue();
                String nlk = exc.getNotLikeValue();
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
        ResultSet row
    ) throws SQLException,
             NoSuchMethodException,
             SecurityException,
             IllegalAccessException,
             IllegalArgumentException,
             InvocationTargetException {
        
        int fieldIndex = 0;
        Map<String, Integer> columnIndexes = new HashMap<>(tableColumns.size());
        Set<String> anonymized = new HashSet<>(tableColumns.size());

        for (Column column : tableColumns) {
            String columnName = column.getName();
            if (anonymized.contains(columnName)) {
                continue;
            }
            int columnIndex = 0;
            if (!columnIndexes.containsKey(columnName)) {
                columnIndex = ++fieldIndex;
                columnIndexes.put(columnName, columnIndex);
            }
            if (isExcludedColumn(db, row, column)) {
                String columnValue = row.getString(columnName);
                updateStmt.setString(columnIndexes.get(columnName), columnValue);
                log.debug("Excluding column: " + columnName + " with value: " + columnValue);
                continue;
            }
            
            anonymized.add(columnName);
            String colValue = callAnonymizingFunctionFor(db, row, column);
            updateStmt.setString(columnIndexes.get(columnName), colValue);
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
    private void anonymizeTable(int batchSize, Connection db, Table table) {
        
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
        
        try {

            db.setAutoCommit(false);
            selectStmt = getSelectQueryStatement(db, table, keyNames, colNames);
            rs = selectStmt.executeQuery();
            
            String updateString = getUpdateQuery(table, colNames, keyNames);
            updateStmt = db.prepareStatement(updateString);
            log.debug("Update SQL: " + updateString);
            
            int batchCounter = 0;
            while (rs.next()) {
                anonymizeRow(updateStmt, tableColumns, keyNames, db, rs);
                batchCounter++;
                if (batchCounter == batchSize) {
                    updateStmt.executeBatch();
                    db.commit();
                    batchCounter = 0;
                }
            }
            
            updateStmt.executeBatch();
            db.commit();
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
    public void anonymize(Properties databaseProperties, Properties anonymizerProperties, Collection<String> tables) 
    throws DatabaseAnonymizerException{

        IDBConnection dbConnection = IDBFactory.get(databaseProperties).createDBConnection();
        Connection connection = dbConnection.connect();

        int batchSize = Integer.parseInt(anonymizerProperties.getProperty("batch_size"));
        Requirement requirement = getRequirement(anonymizerProperties.getProperty("requirement"));

        // Iterate over the requirement
        log.info("Anonymizing data for client " + requirement.getClient() + " Version " + requirement.getVersion());
        for(Table table : requirement.getTables()) {
            if (tables.isEmpty() || tables.contains(table.getName())) {
                anonymizeTable(batchSize, connection, table);
            }
        }
        
        dbConnection.disconnect(connection);
    }
}
