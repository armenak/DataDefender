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
import java.lang.Math;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import static javax.xml.bind.JAXBContext.newInstance;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


/**
 * Entry point for RDBMS data anonymizer
 * 
 * @author Armenak Grigoryan
 */
public class DatabaseAnonymizer implements IAnonymizer { 
    
    private static Logger log = getLogger(DatabaseAnonymizer.class);

    @Override
    public void anonymize(Properties databaseProperties, Properties anonymizerProperties) 
    throws DatabaseAnonymizerException{

        IDBConnection dbConnection = DBConnectionFactory.createDBConnection(databaseProperties);
        Connection connection = dbConnection.connect(databaseProperties);

        String requirementFile = anonymizerProperties.getProperty("requirement");
        int batchSize = parseInt(anonymizerProperties.getProperty("batch_size"));
        
        
        // Collect data from the requirement document
        Requirement requirement = null;
        try {
            JAXBContext jc = newInstance(Requirement.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            requirement = (Requirement) unmarshaller.unmarshal(new FileInputStream(new File(requirementFile)));
        } catch (JAXBException je) {
            log.error(je.toString());
            throw new DatabaseAnonymizerException(je.toString(), je);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(DatabaseAnonymizer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Iterate over the requirement
        log.info("Anonymizing data for client " + requirement.getClient() + " Version " + requirement.getVersion());
        for(Table table : requirement.getTables()) {
            log.info("Table [" + table.getName() + "]. Start ...");
            
            // Start building SQL query
            PreparedStatement pstmt = null;
            Statement stmt          = null;
            ResultSet rs            = null;
            StringBuilder sql       = new StringBuilder("UPDATE " + table.getName() + " SET ");
            int batchCounter        = 0;            
            List<Key> aKeys         = table.getPrimaryKeys();
            String pKey             = table.getPKey();
            Set<String> colNames    = new HashSet<String>();
            Set<String> updateKeys  = new HashSet<String>();
            
            // First iteration over columns to build the UPDATE statement
            String comma = "";
            for (Column column : table.getColumns()) {
                colNames.add(column.getName());
                sql.append(comma).append(column.getName()).append(" = ?");
                comma = ", ";
            }
            
            // Including keys in the updated columns to ensure ON UPDATE doesn't update them and their
            // values don't change without them being meant to
            if (aKeys == null || aKeys.isEmpty()) {
                if (!colNames.contains(pKey)) {
                    sql.append(comma).append(pKey).append(" = ?");
                    updateKeys.add(pKey);
                }
            } else {
                for(Key key : table.getPrimaryKeys()) {
                    sql.append(comma).append(key.getName()).append(" = ?");
                    updateKeys.add(key.getName());
                }
            }
            
            sql.append(" WHERE ");
            if (aKeys == null || aKeys.isEmpty()) {
                sql.append(table.getPKey()).append(" = ?");
            } else {
                String oper = "";
                for(Key key : table.getPrimaryKeys()) {
                    sql.append(oper).append(key.getName()).append(" = ?");
                    oper = " AND ";
                }
            }
            
            // Second iteration over columns to add exeptions (if any)
            for (Column column : table.getColumns()) {
                String exception = column.getException();
                if (exception != null && !exception.equals("")) {
                    String sqlNot = " != ";
                    if (exception.contains("%")) {
                        sqlNot = " NOT LIKE ";
                    }
                    sql.append(" AND ").append(column.getName()).append(sqlNot).append("'").append(exception).append("'");
                }
            }            
            
            final String updateString = sql.toString();
            //log.info(updateString);
            
            try {
                
                StringBuilder keyQuery = new StringBuilder("SELECT ");
                if (aKeys == null || aKeys.isEmpty()) {
                    keyQuery.append(table.getPKey());
                } else {
                    comma = "";
                    for (Key key : aKeys) {
                        keyQuery.append(comma).append(key.getName());
                        comma = ",";
                    }
                }
                keyQuery.append(" FROM ").append(table.getName());
                
                stmt = connection.createStatement();
                rs = stmt.executeQuery(keyQuery.toString());
                pstmt = connection.prepareStatement(updateString);
                
                while (rs.next()) {
                    
                    int index = 0;
                    int nKeys = Math.max((aKeys != null) ? aKeys.size() : 1, 1);
                    int nCols = table.getColumns().size();
                    int keyIndex = 1;
                    
                    String[] aKeyNames = new String[nKeys];
                    String[] aKeyValues = new String[nKeys];
                    if (aKeys == null || aKeys.isEmpty()) {
                        aKeyNames[0] = table.getPKey();
                        aKeyValues[0] = rs.getString(table.getPKey());
                        if (updateKeys.contains(aKeyNames[0])) {
                            pstmt.setString(nCols + keyIndex, aKeyValues[0]);
                            ++keyIndex;
                        }
                    } else {
                        for (int i = 0; i < aKeyValues.length; ++i) {
                            aKeyNames[i] = aKeys.get(i).getName();
                            aKeyValues[i] = rs.getString(aKeyNames[i]);
                            
                            // add to update statement if needed
                            if (updateKeys.contains(aKeyNames[i])) {
                                pstmt.setString(nCols + keyIndex, aKeyValues[i]);
                                ++keyIndex;
                            }
                        }
                    }
                    
                    for (Column column : table.getColumns()) {
                        ++index;
                        if (column.isIgnoreEmpty()) {
                            
                            String colName = column.getName();
                            StringBuilder colQuery = new StringBuilder("SELECT ").append(colName).append(" FROM ").append(table.getName()).append(" WHERE ");
                            
                            String oper = "";
                            for (String name : aKeyNames) {
                                colQuery.append(oper).append(name).append(" = ?");
                                oper = " AND ";
                            }
                            
                            PreparedStatement cStmt = connection.prepareStatement(colQuery.toString());
                            for (int i = 0; i < aKeyValues.length; ++i) {
                                cStmt.setString(i + 1, aKeyValues[i]);
                            }
                            
                            ResultSet cRs = cStmt.executeQuery();
                            if (cRs.next()) {
                                String value = cRs.getString(colName);
                                cStmt.close();
                                cRs.close();
                                if (value == null || value.length() == 0) {
                                    pstmt.setString(index, value);
                                    continue;
                                }
                            }
                        }
                    
                        String function = column.getFunction();
                        if (function == null || function.equals("")) {
                            log.warn("    Function is not defined for column [" + column + "]. Moving to the next column.");
                        } else {
                            try {
                                String className = Utils.getClassName(function);
                                String methodName = Utils.getMethodName(function);
                                Class clazz = Class.forName(className);
                                //Class clazz = com.strider.dataanonymizer.functions.CoreFunctions.class;
                                Object instance = clazz.newInstance();

                                if (column.getParameters() == null) {
                                    Method method = clazz.getMethod(methodName,null);
                                    pstmt.setString(index, method.invoke(instance).toString());
                                } else {
                                    Method method = clazz.getMethod(methodName, String[].class);
                                    String[] stringParams = new String[column.getParameters().size()];
                                    for(int i=0; i<=column.getParameters().size()-1;i++) {
                                        Parameter parameter = column.getParameters().get(i);
                                        stringParams[i] = parameter.getValue().toString();
                                    }
                                    //Object result = method.invoke(null, (Object)stringParams);
                                    Object result = method.invoke(instance, (Object)stringParams);
                                    pstmt.setString(index, result.toString());
                                }
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                log.error(ex.toString());
                                try {
                                    stmt.close();
                                    if (pstmt != null) {
                                        pstmt.close();
                                    }
                                    rs.close();
                                } catch (SQLException sqlex) {
                                    log.error(sqlex.toString());
                                }
                            } catch (InstantiationException | ClassNotFoundException ex) {
                                log.error(ex.toString());
                            }
                        }
                    }
                    
                    index = keyIndex;
                    for (String value : aKeyValues) {
                        pstmt.setString(++index, value);
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
    }
}
