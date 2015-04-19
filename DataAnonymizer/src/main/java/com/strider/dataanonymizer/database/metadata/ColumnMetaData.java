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

package com.strider.dataanonymizer.database.metadata;

/**
 *
 * @author Armenak Grigoryan
 */
public class ColumnMetaData {
    private final String schemaName;
    private final String tableName;
    private final String columnName;
    private final String columnType;

    public ColumnMetaData(final String schemaName, final String tableName, final String columnName, final String columnType) {
        this.schemaName = schemaName;
        this.tableName  = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
    }   
    
    public ColumnMetaData(final String tableName, final String columnName, final String columnType) {
        this.schemaName = null;
        this.tableName  = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getSchemaName() {
        return this.schemaName;
    }
    
    public String getTableName() {
        return this.tableName;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getColumnType() {
        return this.columnType;
    }    
    
    @Override
    public String toString() {
        return this.tableName + "." + this.columnName;
    }
    
    public String toVerboseStr() {
        return toString() + "(" + this.columnType + ")";
    }
}

