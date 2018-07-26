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

package com.strider.datadefender.database;

import com.strider.datadefender.DatabaseDiscoveryException;
import static org.apache.log4j.Logger.getLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.strider.datadefender.database.metadata.IMetaData;
import com.strider.datadefender.database.metadata.MSSQLMetaData;
import com.strider.datadefender.database.metadata.MySQLMetaData;
import com.strider.datadefender.database.metadata.OracleMetaData;
import com.strider.datadefender.database.sqlbuilder.ISQLBuilder;
import com.strider.datadefender.database.sqlbuilder.MSSQLSQLBuilder;
import com.strider.datadefender.database.sqlbuilder.MySQLSQLBuilder;
import com.strider.datadefender.database.sqlbuilder.OracleSQLBuilder;
import com.strider.datadefender.utils.ICloseableNoException;

/**
 * Aggregate all the various db factories.
 * Will handle the 'pooling' of connections (currently only one).
 * All clients should close the connection by calling the close() method of the AutoCloseable interface.
 * @author Akira Matsuo
 */
public interface IDBFactory extends ICloseableNoException {
    
    Connection getConnection();
    Connection getUpdateConnection();
    IMetaData fetchMetaData() throws DatabaseDiscoveryException;
    ISQLBuilder createSQLBuilder();
    String getVendorName();
    
    // Implements the common logic of get/closing of connections
    static abstract class DBFactory implements IDBFactory {
        private static final Logger log = getLogger(DBFactory.class);
        private final Connection connection;
        protected Connection updateConnection;
        private final String vendor;

        DBFactory(String vendorName) throws DatabaseDiscoveryException {
            log.info("Connecting to database");
            connection = createConnection();
            updateConnection = connection;
            vendor = vendorName;
        }
        
        public abstract Connection createConnection() throws DatabaseDiscoveryException;
        @Override
        public Connection getConnection() {
            return connection;
        }
        @Override
        public Connection getUpdateConnection() {
            return updateConnection;
        }
        @Override
        public String getVendorName() {
            return vendor;
        }
        @Override
        public void close() {
            if (connection == null) {
                return;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e);
            }
        }
    }
    
    /**
     * Create db factory for given rdbms. Or illegal argument exception.
     * @param dbProps
     * @return db factory instance
     * @throws DatabaseAnonymizerException 
     */
    static IDBFactory get(final Properties dbProps) throws DatabaseDiscoveryException {
        String vendor = dbProps.getProperty("vendor");
        if ("mysql".equalsIgnoreCase(vendor) || "h2".equalsIgnoreCase(vendor)) {
            return new DBFactory(vendor) {
                {
                    updateConnection = createConnection();
                }
                @Override
                public Connection createConnection() throws DatabaseDiscoveryException {
                    return new MySQLDBConnection(dbProps).connect();
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseDiscoveryException {
                    return new MySQLMetaData(dbProps, getConnection());
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new MySQLSQLBuilder(dbProps);
                }
            };
        } else if ("mssql".equalsIgnoreCase(vendor)){
            return new DBFactory(vendor) {
                @Override
                public Connection createConnection() throws DatabaseDiscoveryException {
                    return new MSSQLDBConnection(dbProps).connect();
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseDiscoveryException {
                    return new MSSQLMetaData(dbProps, getConnection());
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new MSSQLSQLBuilder(dbProps);
                }
            };
        } else if ("oracle".equalsIgnoreCase(vendor)) {
            return new DBFactory(vendor) {
                @Override
                public Connection createConnection() throws DatabaseDiscoveryException {
                    return new OracleDBConnection(dbProps).connect();
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseDiscoveryException {
                    return new OracleMetaData(dbProps, getConnection());
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new OracleSQLBuilder(dbProps);
                }
            };
        }
        
        throw new IllegalArgumentException("Database " + vendor + " is not supported");
    }
}

