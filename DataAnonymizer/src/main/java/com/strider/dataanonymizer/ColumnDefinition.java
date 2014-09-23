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
package com.strider.dataanonymizer;

/**
 *
 * @author Armenak Grigoryan
 */
public class ColumnDefinition {
    private String tableName;
    private String columnName;
    private String functionName;
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableName() {
        return this.tableName;
    }
    
    public void setColumnName(String columnName) {    
        this.columnName = columnName;
    }
    
    public String getColumnName() {
        return this.columnName;
    }    
    
    public void setFunctionName(String functionName) {    
        this.functionName = functionName;
    }
    
    public String getFunctionName() {
        return this.functionName;
    }        
    
    @Override
    public String toString() {
        return "Table name:" + this.tableName + 
               " Column name: " + this.columnName + 
               " Function name: " + this.functionName; 
    }
}
