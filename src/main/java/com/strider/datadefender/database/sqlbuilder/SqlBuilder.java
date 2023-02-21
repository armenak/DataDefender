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
import com.strider.datadefender.DbConfig.Vendor;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;

/**
 * Provides 'default' implementation which can be overridden.
 * @author Akira Matsuo
 */
@Log4j2
@RequiredArgsConstructor
public class SqlBuilder implements ISqlBuilder {

    protected final DbConfig config;

    /**
     * Appends "LIMIT {n}" to the end of the query.
     *
     * @param sqlString
     * @param limit
     * @return
     */
    @Override
    public String buildSelectWithLimit(final String sqlString, final int limit) {
        final StringBuilder sql = new StringBuilder(sqlString);
        if (limit != 0) {
            sql.append(" LIMIT ").append(limit);
        }
        log.debug("Query after adding limit: [{}]", sql);
        return sql.toString();
    }

    /**
     * Prepends the schema name followed by a single "." to the passed tableName
     * if a schema name is configured.
     *
     * @param tableName
     * @return
     */
    @Override
    public String prefixSchema(final String tableName) {
        final String schema = config.getSchema();
        if (StringUtils.isNotBlank(schema)) {
            if(config.getVendor() == Vendor.POSTGRESQL) {
                return schema + "." + "\"" + tableName + "\"";
            }else {
                return schema + "." + tableName;
            }
        }
        return tableName;
    }
    
    /**
     * Method to get DB Vendor
     * @return
     */
    @Override
    public Vendor getVendor() {
        return config.getVendor();
    }
}