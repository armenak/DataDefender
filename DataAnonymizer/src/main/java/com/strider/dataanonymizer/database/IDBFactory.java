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

import java.util.Properties;

import com.strider.dataanonymizer.database.metadata.IMetaData;
import com.strider.dataanonymizer.database.metadata.MSSQLMetaData;
import com.strider.dataanonymizer.database.metadata.MySQLMetaData;
import com.strider.dataanonymizer.database.metadata.OracleMetaData;
import com.strider.dataanonymizer.database.sqlbuilder.ISQLBuilder;
import com.strider.dataanonymizer.database.sqlbuilder.MSSQLSQLBuilder;
import com.strider.dataanonymizer.database.sqlbuilder.MySQLSQLBuilder;
import com.strider.dataanonymizer.database.sqlbuilder.OracleSQLBuilder;

/**
 * Aggregate all the various db factories.
 * 
 * @author Akira Matsuo
 */
public interface IDBFactory {
    
    IDBConnection createDBConnection() throws DatabaseAnonymizerException;
    IMetaData fetchMetaData() throws DatabaseAnonymizerException;
    ISQLBuilder createSQLBuilder();
    
    /**
     * Create db factory for given rdbms. Or illegal argument exception.
     * @param dbProps
     * @return db factory instance
     */
    static IDBFactory get(final Properties dbProps) {
        String vendor = dbProps.getProperty("vendor");
        if ("mysql".equalsIgnoreCase(vendor) || "h2".equalsIgnoreCase(vendor)) {
            return new IDBFactory() {
                @Override
                public IDBConnection createDBConnection() throws DatabaseAnonymizerException {
                    return new MySQLDBConnection(dbProps);
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseAnonymizerException {
                    return new MySQLMetaData(dbProps);
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new MySQLSQLBuilder();
                }
            };
        } else if ("mssql".equalsIgnoreCase(vendor)){
            return new IDBFactory() {
                @Override
                public IDBConnection createDBConnection() throws DatabaseAnonymizerException {
                    return new MSSQLDBConnection(dbProps);
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseAnonymizerException {
                    return new MSSQLMetaData(dbProps);
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new MSSQLSQLBuilder();
                }
            };
        } else if ("oracle".equalsIgnoreCase(vendor)) {
            return new IDBFactory() {
                @Override
                public IDBConnection createDBConnection() throws DatabaseAnonymizerException {
                    return new OracleDBConnection(dbProps);
                }
                @Override
                public IMetaData fetchMetaData() throws DatabaseAnonymizerException {
                    return new OracleMetaData(dbProps);
                }
                @Override
                public ISQLBuilder createSQLBuilder() {
                    return new OracleSQLBuilder();
                }
            };
        }
        
        throw new IllegalArgumentException("Database " + vendor + " is not supported");
    }
    
}

