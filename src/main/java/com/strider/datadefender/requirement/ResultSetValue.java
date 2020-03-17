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
package com.strider.datadefender.requirement;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Zaahid Bateson
 */
@Data
@Log4j2
public class ResultSetValue {

    private Class type;
    private String columnName;

    public ResultSetValue(Class type, String columnName) {
        this.type = type;
        this.columnName = columnName;
    }

    public Object getValue(ResultSet rs) throws SQLException {
        if (type == ResultSet.class) {
            return rs;
        }
        return rs.getObject(columnName, type);
    }
}
