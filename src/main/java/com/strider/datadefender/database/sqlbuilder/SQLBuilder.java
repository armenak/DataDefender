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
package com.strider.datadefender.database.sqlbuilder;

import java.util.Properties;

import com.strider.datadefender.utils.CommonUtils;

/**
 * Provides 'default' implementation which can be overridden.
 * @author Akira Matsuo
 */
public abstract class SQLBuilder implements ISQLBuilder {
    /* changed to public to allow use o databaseProperties object in MSSQLSQLBuilder.java */
    public final Properties databaseProperties;

    protected SQLBuilder(final Properties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    @Override
    public String buildSelectWithLimit(final String sqlString, final int limit) {
        final StringBuilder sql = new StringBuilder(sqlString);

        if (limit != 0) {
            sql.append(" LIMIT ").append(limit);
        }
        return sql.toString();
    }

    @Override
    public String prefixSchema(final String tableName) {
        final String schema = databaseProperties.getProperty("schema");
        String prefixAndTableName;

        if (CommonUtils.isEmptyString(schema)) {
            prefixAndTableName = tableName;
            return tableName;
        } else {
            prefixAndTableName = schema + "." + tableName;
        }

        return prefixAndTableName;
    }

}
