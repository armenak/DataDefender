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
 */
package com.strider.datadefender.database.sqlbuilder;

import com.strider.datadefender.DbConfig;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * MS SQL Server implementation of the ISqlBuilder
 *
 * @author Armenak Grigoryan
 * @author Luis Marques
 */
@Log4j2
public class MsSqlBuilder extends SqlBuilder {

    public MsSqlBuilder(DbConfig config) {
        super(config);
    }

    /**
     * Uses TOP to limit the passed query.
     *
     * @param sqlString
     * @param limit
     * @return
     */
    @Override
    public String buildSelectWithLimit(final String sqlString, final int limit) {
        String query = sqlString.replaceFirst("(?i)(SELECT)(\\s+)", "$1 TOP " + limit + " $2");
        log.debug("Query after adding limit: [{}]", query);
        return query;
    }

    /**
     * Prefixes the schema name, followed by a "." character if set.
     * Additionally surrounds schema name (if set) and tableName with square
     * brackets, so the end result is either "[tableName]" or
     * "[schemaName].[tableName]".
     *
     * @param tableName
     * @return
     */
    @Override
    public String prefixSchema(final String tableName) {
        final String schema = config.getSchema();
        if (StringUtils.isNotBlank(schema)) {
            return "[" + schema + "].[" + tableName + "]";
        }
        return "[" + tableName + "]";
    }
}
