package com.strider.dataanonymizer;

/**
 *
 * @author Armenak Grigoryan
 */
public class ColumnMetaData {
    private String tableName;
    private String columnName;
    private String columnType;

    ColumnMetaData(String tableName, String columnName, String columnType) {
        this.tableName  = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
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
}

