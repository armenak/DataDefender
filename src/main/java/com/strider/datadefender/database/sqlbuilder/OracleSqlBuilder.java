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
 *
 */
package com.strider.datadefender.database.sqlbuilder;

import com.strider.datadefender.DbConfig;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Oracle implementation of the ISqlBuilder
 *
 * @author Armenak Grigoryan
 */
@Log4j2
public class OracleSqlBuilder extends SqlBuilder {

    public OracleSqlBuilder(DbConfig config) {
        super(config);
    }

    /**
     * Uses ROWNUM to limit the passed query.
     *
     * @param sqlString
     * @param limit
     * @return 
     */
    @Override
    public String buildSelectWithLimit(final String sqlString, final int limit) {
        String sql = "SELECT q.* FROM (" + StringUtils.stripEnd(sqlString.trim(), ";") + ") q WHERE ROWNUM <= " + limit;
        log.debug("Query after adding limit: [{}]", sql);
        return sql;
    }
}
