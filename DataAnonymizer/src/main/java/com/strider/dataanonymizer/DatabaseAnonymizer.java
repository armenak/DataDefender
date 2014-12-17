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
            
            // First iteration over columns to build the UPDATE statement
            for(Column column : table.getColumns()) {
                sql.append(column.getName()).append(" = ?,");
            }
            // remove training ","
            if (sql.length() > 0) {
                sql.setLength(sql.length() - 1);
            }
            
            sql.append(" WHERE ").append(table.getPKey()).append(" = ?");
            
            // Second iteration over columns to add exeptions (if any)
            for(Column column : table.getColumns()) {
                String exception = column.getException();
                if (exception != null && !exception.equals("")) {
                    String sqlNot = " != ";
                    if (exception.contains("%")) {
                        sqlNot = " NOT LIKE ";
                    }
                    sql.append( " AND ").append(column.getName()).append(sqlNot).append("'").append(exception).append("'");
                }
                
            }            
            
            final String updateString = sql.toString();
            //log.info(updateString);
            
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(String.format("SELECT %s FROM %s ", table.getPKey(), table.getName()));
                pstmt = connection.prepareStatement(updateString);
                while (rs.next()) {
                    int id = rs.getInt(table.getPKey());
                    int index = 0;
                    
                    for(Column column : table.getColumns()) {
                    
                        if (column.isIgnoreEmpty()) {
                            String colName = column.getName();
                            Statement cStmt = connection.createStatement();
                            ResultSet cRs = cStmt.executeQuery(String.format("SELECT %s FROM %s WHERE %s = %d", colName, table.getName(), table.getPKey(), id));
                            if (!cRs.next()) {
                                pstmt.setString(++index, "");
                                continue;
                            }
                            String value = cRs.getString(colName);
                            if (value == null || value.length() == 0) {
                                pstmt.setString(++index, "");
                                continue;
                            }
                            cStmt.close();
                            cRs.close();
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
                                    pstmt.setString(++index, method.invoke(instance).toString());
                                } else {
                                    Method method = clazz.getMethod(methodName, String[].class);
                                    String[] stringParams = new String[column.getParameters().size()];
                                    for(int i=0; i<=column.getParameters().size()-1;i++) {
                                        Parameter parameter = column.getParameters().get(i);
                                        stringParams[i] = parameter.getValue().toString();
                                    }
                                    //Object result = method.invoke(null, (Object)stringParams);
                                    Object result = method.invoke(instance, (Object)stringParams);
                                    pstmt.setString(++index, result.toString());
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
                    pstmt.setInt(++index, id);
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
