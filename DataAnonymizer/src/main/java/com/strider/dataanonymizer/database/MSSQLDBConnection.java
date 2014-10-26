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
import java.sql.Connection;
import static java.sql.DriverManager.getConnection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author sdi
 */
public class MSSQLDBConnection extends DBConnection {
    private static final Logger log = getLogger(MSSQLDBConnection.class);
    
    /**
     * Establishes database connection
     * @param properties
     * @return Connection
     * @throws DatabaseAnonymizerException 
     */
    @Override
    public Connection connect(Properties properties) throws DatabaseAnonymizerException {

        String driver = properties.getProperty("driver");
        String vendor = properties.getProperty("vendor");
        String url = properties.getProperty("url");
        String userName = properties.getProperty("username");
        String password = properties.getProperty("password");
        log.debug("Using driver " + driver);
        log.debug("Database type: " + vendor);
        log.debug("Database URL: " + url);
        log.debug("Logging in using username " + userName); 
        
        try {
            forName(driver);
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.toString());
            throw new DatabaseAnonymizerException(cnfe.toString(), cnfe);
        }
        
        StringBuilder sqlServerURL = new StringBuilder(url);
        sqlServerURL.append(";user=").append(userName).append(";password=").append(password);
        Connection conn = null;
        try {
            log.info("Connecting to database " + url);
            conn = getConnection(sqlServerURL.toString());
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
