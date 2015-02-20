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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import static java.lang.Integer.parseInt;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import static javax.xml.bind.JAXBContext.newInstance;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


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
    private String getSelectQuery(String tableName, Collection<String> columns) {
        return String.format(
            "SELECT %s FROM %s",
            StringUtils.join(columns, ", "),
            tableName
        );
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
    private String getAnonymizedValueForColumn(Column column, Connection dbConn)
        throws NoSuchMethodException,
               SecurityException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException {
        
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
            List<String> stringParams = new ArrayList<String>(parms.size());
            for (Parameter parm : parms) {
                stringParams.add(parm.getValue());
            }
            
            Method method = clazz.getMethod(methodName, (stringParams.size() != 0) ? String[].class : null);
            return method.invoke(instance, (Object) stringParams.toArray(new String[stringParams.size()])).toString();
            
        } catch (InstantiationException | ClassNotFoundException ex) {
            log.error(ex.toString());
        }
        
        return "";
    }
    
    /**
     * Anonymization function for a single table.
     * 
     * Sets up queries, loops over columns and anonymizes columns for the passed
     * Table.
     * 
     * @param table 
     */
    private void anonymizeTable(int batchSize, Connection connection, Table table) {
        
        log.info("Table [" + table.getName() + "]. Start ...");
        
        List<Column> tableColumns = table.getColumns();
        PreparedStatement pstmt = null;
        Statement stmt          = null;
        ResultSet rs            = null;
        int batchCounter        = 0;
        
        // colNames is looked up with contains, and iterated over.  Using LinkedHashSet means
        // duplicate column names won't be added to the query, so a check in the column loop
        // below was created to ensure a reasonable warning message is logged if that happens.
        Set<String> colNames = new LinkedHashSet<>(tableColumns.size());
        // keyNames is only iterated over, so no need for a hash set
        List<String> keyNames = new LinkedList<>();
        // updateKeys is used for contains() and is added to allColumns (which needs to be in
        // a predictable order) - so it needs to be a LinkedHashSet
        Set<String> updateKeys = new LinkedHashSet<>();

        fillColumnNames(table, colNames);
        fillPrimaryKeyNamesList(table, keyNames);
        fillUpdatableKeys(keyNames, colNames, updateKeys);
        
        List<String> allColumns = new LinkedList<>();
        allColumns.addAll(colNames);
        allColumns.addAll(updateKeys);
        
        String selectQuery = getSelectQuery(table.getName(), allColumns);
        
        try {

            stmt = connection.createStatement();
            rs = stmt.executeQuery(selectQuery);
            
            String updateString = getUpdateQuery(table, allColumns, keyNames);
            pstmt = connection.prepareStatement(updateString);
            log.debug(updateString);
            
            while (rs.next()) {

                int fieldIndex = 0;
                Set<String> updatedColumns = new HashSet<>(tableColumns.size());
                for (Column column : tableColumns) {
                    
                    String columnName = column.getName();
                    String columnValue = rs.getString(columnName);
                    if (updatedColumns.contains(columnName)) {
                        log.warn("Column " + columnName + " is declared more than once - ignoring second definition");
                        continue;
                    }
                    
                    updatedColumns.add(columnName);
                    ++fieldIndex;
                    
                    if (column.isIgnoreEmpty()) {
                        if (columnValue == null || columnValue.length() == 0) {
                            pstmt.setString(fieldIndex, columnValue);
                            continue;
                        }
                    }
                    try {
                        String value = "";
                        pstmt.setString(fieldIndex, value = getAnonymizedValueForColumn(column, connection));
                        log.debug(fieldIndex + " = " + value);
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        
                        log.error(ex.toString());
                        try {
                            stmt.close();
                            if (pstmt != null) {
                                pstmt.close();
                            }
                            rs.close();
                            return;
                        } catch (SQLException sqlex) {
                            log.error(sqlex.toString());
                        }
                    }
                }
                
                int nUpdateKeys = updateKeys.size();
                int whereIndex = nUpdateKeys + fieldIndex;
                for (String key : keyNames) {
                    String value = rs.getString(key);
                    pstmt.setString(++whereIndex, value);
                    log.debug(whereIndex + " = " + value);
                    if (updateKeys.contains(key)) {
                        pstmt.setString(++fieldIndex, value);
                        log.debug(fieldIndex + " = " + value);
                    }
                }
                
                pstmt.addBatch();
                batchCounter++;
                if (batchCounter == batchSize) {
                    pstmt.executeBatch();
                    connection.commit();
                    batchCounter = 0;
                }
            }
            
            pstmt.executeBatch();
            connection.commit();
            stmt.close();
            pstmt.close();
            rs.close();
            
        } catch (SQLException sqle) {
            log.error(sqle.toString());
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (pstmt != null) {
                    pstmt.close();
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
