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

import java.util.Properties;

/**
 * @author Armenak Grigoryan
 */
public class OracleSQLBuilder extends SQLBuilder{
    
    public OracleSQLBuilder(final Properties databaseProperties) {
        super(databaseProperties);
    }

    @Override
    public String buildSelectWithLimit(final String sqlString, final int limit) {
        final StringBuilder sql = new StringBuilder(sqlString);
        
        if (limit != 0) {
            sql.append(" AND rownum <= ").append(limit);
        }
        return sql.toString();
    }
    
}
