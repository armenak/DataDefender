package com.strider.dataanonymizer;

/**
 *
 * @author strider
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
