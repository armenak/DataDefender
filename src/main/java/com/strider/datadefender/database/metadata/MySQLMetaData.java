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
package com.strider.datadefender.database.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author armenak
 */
public class MySQLMetaData extends MetaData {
    
    public MySQLMetaData(Properties databaseProperties, Connection connection) {
        super(databaseProperties, connection);
        // Prevent MatchMetaData to be populated with invalid schema property
        // since schema property not supported by MySQL database.        
        if (schema != null) {
            schema = null;
        }
    }

    @Override
    protected ResultSet getTableRS(DatabaseMetaData md) throws SQLException {
        return md.getTables(null, null, "%", new String[] {"TABLE"});
    }
    @Override
    protected ResultSet getPKRS(DatabaseMetaData md, String tableName) throws SQLException {
        return md.getPrimaryKeys(null, null, tableName);
    }
    @Override
    protected ResultSet getColumnRS(DatabaseMetaData md, String tableName) throws SQLException {
        return md.getColumns(null, null, tableName, null);
    }
    @Override
    protected String getColumnName(ResultSet columnRS) throws SQLException {
        return columnRS.getString("COLUMN_NAME");
    }
}