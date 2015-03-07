/*
 * Copyright 2015, Armenak Grigoryan, and individual contributors as indicated
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

package com.strider.dataanonymizer.database.sqlbuilder;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import java.util.Properties;

/**
 *
 * @author Armenak Grigoryan
 */
public class SQLBuilderFactory {
    
    public static ISQLBuilder createSQLBuilder(final Properties databaseProperties) 
    throws DatabaseAnonymizerException {
        
        String vendor = databaseProperties.getProperty("vendor");
        if (vendor.equalsIgnoreCase("mysql")){
            return new MySQLSQLBuilder();
        } else if (vendor.equalsIgnoreCase("mssql")){
            return new MSSQLSQLBuilder();
        } else if (vendor.equalsIgnoreCase("oracle")) {
            return new OracleSQLBuilder();
        }
        
        throw new IllegalArgumentException("Database " + vendor + " is not supported");
    }
}
