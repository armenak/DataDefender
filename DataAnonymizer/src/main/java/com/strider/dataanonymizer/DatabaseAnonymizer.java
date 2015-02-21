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
import com.strider.dataanonymizer.functions.CoreFunctions;
import com.strider.dataanonymizer.functions.Utils;
import com.strider.dataanonymizer.requirement.Column;
import com.strider.dataanonymizer.requirement.Parameter;
import com.strider.dataanonymizer.requirement.Requirement;
import com.strider.dataanonymizer.requirement.Key;
import com.strider.dataanonymizer.requirement.Table;
import com.strider.dataanonymizer.requirement.Exclude;
import com.strider.dataanonymizer.utils.LikeMatcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import static java.lang.Integer.parseInt;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import static javax.xml.bind.JAXBContext.newInstance;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import java.util.Arrays;

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
     * Adds primary key column names from keys into the 'updatable' collection
     * if they are not in the columns collection from the table.
     * 
     * The list of key columns that can be updated consists of columns that have
     * not been specified by the requirements as a column that needs to be
     * anonymized.  It is used to avoid ON UPDATE statements from being
     * triggered for those columns.
     * 
     * @param keys
     * @param columns
     * @param updatable 
     */
    private void fillUpdatableKeys(final Collection<String> keys, final Collection<String> columns, Collection<String> updatable) {
        for (String key : keys) {
            if (!columns.contains(key)) {
                updatable.add(key);
            }
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
     * @param columns
     * @return 
     */
    private PreparedStatement getSelectQueryStatement(Connection db, Table table, Collection<String> columns) throws SQLException {
        
        List<String> params = new LinkedList<>();
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(StringUtils.join(columns, ", ")).append(" FROM ").append(table.getName());
        
        List<Exclude> exclusions = table.getExclusions();
        if (exclusions != null) {
            String separator = " WHERE ";
            for (Exclude exc : exclusions) {
                String eq = exc.getEqualsValue();
                String lk = exc.getLikeValue();
                boolean nl = exc.isExcludeNulls();
                String col = exc.getName();

                if (col != null && col.length() != 0) {
                    if (eq != null) {
                        query.append(separator).append(col).append(" != ?");
                        params.add(eq);
                        separator = " AND ";
                    }
                    if (lk != null && lk.length() != 0) {
                        query.append(separator).append(col).append(" NOT LIKE ?");
                        params.add(lk);
                        separator = " AND ";
                    }
                    if (nl) {
                        query.append(separator).append(col).append(" IS NOT NULL");
                        separator = " AND ";
                    }
                }
            }
        }
        
        PreparedStatement stmt = db.prepareStatement(query.toString());
        int paramIndex = 1;
        for (String param : params) {
            stmt.setString(paramIndex, param);
            ++paramIndex;
        }
        
        return stmt;
    }
    
    /**
     * Calls the anonymization function for the given Column, and returns its
     * anonymized value.
     * 
     * @param column
     * @param dbConn
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    private String callAnonymizingFunctionFor(ResultSet row, Column column, Connection dbConn)
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
            Class clazz = Class.forName(className);
            
            CoreFunctions instance = (CoreFunctions) clazz.newInstance();
            instance.setDatabaseConnection(dbConn);

            List<Parameter> parms = column.getParameters();
            Map<String, Parameter> stringParams = new HashMap<>(parms.size());
            for (Parameter parm : parms) {
                stringParams.put(parm.getName(), parm);
            }
            
            List<Object> parameterValues = new ArrayList<>(parms.size());
            Method[] methods = clazz.getMethods();
            Method selectedMethod = null;
            
            methodLoop:
            for (Method m : methods) {
                if (m.getName().equals(methodName) && m.getReturnType() == String.class) {
                    java.lang.reflect.Parameter[] mParams = m.getParameters();
                    parameterValues.clear();
                    for (java.lang.reflect.Parameter par : mParams) {
                        
                        // need a better place for this
                        if (par.getName().equals("value")) {
                            parameterValues.add(columnValue);
                            continue;
                        } else if (par.getName().equals("row") && par.getType() == ResultSet.class) {
                            parameterValues.add(row);
                            continue;
                        }
                        
                        if (!stringParams.containsKey(par.getName())) {
                            continue methodLoop;
                        }
                        
                        Parameter colPar = stringParams.get(par.getName());
                        Class fnParameterType = par.getType();
                        String columnTypeName = colPar.getType();
                        String argumentValue = colPar.getValue();
                        
                        // if not defined specifically, use whatever's defined
                        if (columnTypeName == null) {
                            columnTypeName = fnParameterType.getName();
                        }
                        
                        if (fnParameterType == byte.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Byte(Byte.parseByte(argumentValue)));
                        } else if (fnParameterType == short.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Short(Short.parseShort(argumentValue)));
                        } else if (fnParameterType == char.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Character(argumentValue.charAt(0)));
                        } else if (fnParameterType == int.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Integer(Integer.parseInt(argumentValue)));
                        } else if (fnParameterType == long.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Long(Long.parseLong(argumentValue)));
                        } else if (fnParameterType == float.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Float(Float.parseFloat(argumentValue)));
                        } else if (fnParameterType == double.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Double(Double.parseDouble(argumentValue)));
                        } else if (fnParameterType == boolean.class && columnTypeName.equals(fnParameterType.getName())) {
                            parameterValues.add(new Boolean(Boolean.parseBoolean(argumentValue)));
                        } else if ((fnParameterType == String.class) && (columnTypeName.equals(String.class.getName()) || columnTypeName.equals("String"))) {
                            parameterValues.add(argumentValue);
                        } else if (fnParameterType.getName().equals(columnTypeName)) {
                            Class parClass = Class.forName(columnTypeName);
                            Constructor constr = parClass.getConstructor(String.class);
                            parameterValues.add(constr.newInstance(argumentValue));
                        }
                    }
                    
                    // actual parameters check less than xml defined parameters size, because values could be auto-assigned (like 'values' and 'row' params)
                    if (parameterValues.size() != mParams.length || parameterValues.size() < stringParams.size()) {
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
            
            log.debug("Anonymizing function: " + methodName + " with parameters: " + Arrays.toString(parameterValues.toArray()));
            return selectedMethod.invoke(instance, parameterValues.toArray()).toString();
            
        } catch (InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }
        
        return "";
    }
    
    /**
     * Returns the anonymized value of a column, or its current value if it
     * should be excluded.
     * 
     * Checks for exclusions against the current row and column values, either
     * returning the column's current value or returning an anonymized value by
     * calling callAnonymizingFunctionFor.
     * 
     * @param db
     * @param row
     * @param column
     * @return the columns value
     * @throws SQLException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    private String getAnonymizedColumnValue(Connection db, ResultSet row, Column column)
        throws SQLException,
               NoSuchMethodException,
               SecurityException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException {
        
        String columnName = column.getName();
        String columnValue = row.getString(columnName);
        
        List<Exclude> exclusions = column.getExclusions();
        if (exclusions != null) {
            for (Exclude exc : exclusions) {
                String name = exc.getName();
                String eq = exc.getEqualsValue();
                String lk = exc.getLikeValue();
                boolean nl = exc.isExcludeNulls();
                if (name == null || name.length() == 0) {
                    name = columnName;
                }
                String testValue = row.getString(name);

                if (nl && testValue == null) {
                    return columnValue;
                } else if (eq != null && testValue.equals(eq)) {
                    return columnValue;
                } else if (lk != null && lk.length() != 0) {
                    LikeMatcher matcher = new LikeMatcher(lk);
                    if (matcher.matches(testValue)) {
                        return columnValue;
                    }
                }
            }
        }

        return callAnonymizingFunctionFor(row, column, db);
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
     * @param updateKeys
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
        Collection<String> updateKeys,
        Connection db,
        ResultSet row
    ) throws SQLException,
             NoSuchMethodException,
             SecurityException,
             IllegalAccessException,
             IllegalArgumentException,
             InvocationTargetException {
        
        int fieldIndex = 0;
        Set<String> updatedColumns = new HashSet<>(tableColumns.size());

        for (Column column : tableColumns) {
            String columnName = column.getName();
            if (updatedColumns.contains(columnName)) {
                log.warn("Column " + columnName + " is declared more than once - ignoring second definition");
                continue;
            }
            updatedColumns.add(columnName);
            ++fieldIndex;
            
            String colValue = getAnonymizedColumnValue(db, row, column);
            updateStmt.setString(fieldIndex, colValue);
        }

        int nUpdateKeys = updateKeys.size();
        int whereIndex = nUpdateKeys + fieldIndex;
        for (String key : keyNames) {
            String value = row.getString(key);
            updateStmt.setString(++whereIndex, value);
            if (updateKeys.contains(key)) {
                updateStmt.setString(++fieldIndex, value);
            }
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
        // updateKeys is used for contains() and is added to allColumns (which needs to be in
        // a predictable order) - so it needs to be a LinkedHashSet
        Set<String> updateKeys = new LinkedHashSet<>();
        // exceptCols is added as query params

        fillColumnNames(table, colNames);
        fillPrimaryKeyNamesList(table, keyNames);
        fillUpdatableKeys(keyNames, colNames, updateKeys);
        
        List<String> allColumns = new LinkedList<>();
        allColumns.addAll(colNames);
        allColumns.addAll(updateKeys);
        
        // required in this scope for 'catch' block
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        
        try {

            selectStmt = getSelectQueryStatement(db, table, allColumns);
            rs = selectStmt.executeQuery();
            
            String updateString = getUpdateQuery(table, allColumns, keyNames);
            updateStmt = db.prepareStatement(updateString);
            log.debug("Update SQL: " + updateString);
            
            int batchCounter = 0;
            while (rs.next()) {
                anonymizeRow(updateStmt, tableColumns, keyNames, updateKeys, db, rs);
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
    public void anonymize(Properties databaseProperties, Properties anonymizerProperties) 
    throws DatabaseAnonymizerException{

        IDBConnection dbConnection = DBConnectionFactory.createDBConnection(databaseProperties);
        Connection connection = dbConnection.connect(databaseProperties);

        int batchSize = parseInt(anonymizerProperties.getProperty("batch_size"));
        Requirement requirement = getRequirement(anonymizerProperties.getProperty("requirement"));

        // Iterate over the requirement
        log.info("Anonymizing data for client " + requirement.getClient() + " Version " + requirement.getVersion());
        for(Table table : requirement.getTables()) {
            anonymizeTable(batchSize, connection, table);
        }
        
        dbConnection.disconnect(connection);
    }
}
