/*
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
 */
package com.strider.datadefender.database.metadata;

import com.strider.datadefender.DbConfig;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Overridden to omit using schema in some cases, as it's not supported.
 * @author armenak
 */
public class MySQLMetaData extends MetaData {

    public MySQLMetaData(DbConfig config, Connection connection) {
        super(config, connection);
    }

    @Override
    protected ResultSet getTableResultSet(final DatabaseMetaData md) throws SQLException {
        return md.getTables(null, null, "%", new String[] { "TABLE" });
    }

    @Override
    protected ResultSet getColumnResultSet(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getColumns(null, null, tableName, null);
    }

    @Override
    protected ResultSet getForeignKeysResultSet(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getImportedKeys(null, null, tableName);
    }

    @Override
    protected ResultSet getPrimaryKeysResultSet(final DatabaseMetaData md, final String tableName) throws SQLException {
        return md.getPrimaryKeys(null, null, tableName);
    }
}
