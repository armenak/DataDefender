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

package com.strider.dataanonymizer.database;

import static java.lang.Class.forName;
import static org.apache.log4j.Logger.getLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.strider.dataanonymizer.utils.ISupplierWithException;

/**
 * Abstract class handling database connections
 */
public abstract class DBConnection implements IDBConnection {
    private static final Logger log = getLogger(DBConnection.class);

    protected final String driver;
    protected final String vendor;
    protected final String url;
    protected final String userName;
    protected final String password;
    
    /**
     * Default constructor, initializes connection properties and loads db class.
     * @param properties
     * @throws DatabaseAnonymizerException
     */
    public DBConnection(Properties properties) throws DatabaseAnonymizerException {
        driver   = properties.getProperty("driver");
        vendor   = properties.getProperty("vendor");
        url      = properties.getProperty("url");
        userName = properties.getProperty("username");
        password = properties.getProperty("password");
        
        log.info("Database vendor: " + vendor);
        log.info("Using driver " + driver);
        log.info("Database URL: " + url);
        log.info("Logging in using username " + userName); 
        
        try {
            log.info("Loading database driver");
            forName(driver);
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.toString());
            throw new DatabaseAnonymizerException(cnfe.toString(), cnfe);
        }
    }

    /**
     * Handles the actual creating of the connection, but running the supplier function provided by subclasses.
     * @param supplier
     * @return
     * @throws DatabaseAnonymizerException
     */
    protected Connection doConnect(ISupplierWithException<Connection, SQLException> supplier) throws DatabaseAnonymizerException {
        Connection conn = null;
        try {
            log.info("Establishing database connection");
            conn = supplier.get();
            conn.setAutoCommit(false);
        } catch (SQLException sqle) {
            log.error(sqle.toString());
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sql) {
                    log.error(sql.toString());
                }
            }
            throw new DatabaseAnonymizerException(sqle.toString(), sqle);
        }
        return conn;
    }
}

