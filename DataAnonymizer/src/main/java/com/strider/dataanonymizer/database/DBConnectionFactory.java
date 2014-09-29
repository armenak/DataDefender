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
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author Armenak Grigoryan
 */
public class DBConnectionFactory {
    
    private static final Logger log = getLogger(DBConnectionFactory.class);
    
    public static IDBConnection createDBConnection(final Properties databaseProperties) 
    throws DatabaseAnonymizerException {
        
        String vendor = databaseProperties.getProperty("vendor");
        if (vendor.equalsIgnoreCase("mysql")){
            return new MySQLDBConnection();
        } else if (vendor.equalsIgnoreCase("mssql")){
            return new MSSQLDBConnection();
        } else if (vendor.equalsIgnoreCase("oracle")) {
            return new OracleDBConnection();
        }
        
        throw new IllegalArgumentException("Database " + vendor + " is not supported");
    }
}
