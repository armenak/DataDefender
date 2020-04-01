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
package com.strider.datadefender.database.metadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Holds metadata for a table.
 *
 * @author Armenak Grigoryan
 */
@Data
@Log4j2
public class TableMetaData implements Comparable<TableMetaData> {

    @Data
    public class ColumnMetaData implements Comparable<ColumnMetaData> {

        private final int columnIndex;
        private final String columnName;
        private final Class  columnType;
        private final int columnSize;
        private final boolean isPrimaryKey;
        private final boolean isForeignKey;
        private final String foreignKeyReference;

        @Override
        public int compareTo(ColumnMetaData t) {
            return Comparator
                .comparing(ColumnMetaData::getColumnIndex)
                .compare(this, t);
        }

        @Override
        public String toString() {
            return TableMetaData.this.toString() + "." + columnName;
        }

        public TableMetaData getTable() {
            return TableMetaData.this;
        }
    }

    private final String schemaName;
    private final String tableName;
    @Setter(AccessLevel.NONE)
    private List<ColumnMetaData> columns = new ArrayList<>();

    /**
     * Filters and returns a list of columns that are designated as primary
     * keys.
     * @return
     */
    public List<ColumnMetaData> getPrimaryKeys() {
        return columns.stream().filter((c) -> c.isPrimaryKey).collect(Collectors.toList());
    }

    /**
     * Filters and returns a list of columns that are designated as foreign
     * keys.
     * @return 
     */
    public List<ColumnMetaData> getForeignKeys() {
        return columns.stream().filter((c) -> c.isForeignKey).collect(Collectors.toList());
    }

    /**
     * Adds a ColumnMetaData to the list of columns.
     *
     * @param columnIndex
     * @param columnName
     * @param columnType
     * @param columnSize
     * @param isPrimaryKey
     * @param foreignKeyReference
     */
    public void addColumn(
        int columnIndex,
        String columnName,
        Class columnType,
        int columnSize,
        boolean isPrimaryKey,
        String foreignKeyReference
    ) {
        columns.add(new ColumnMetaData(
            columnIndex,
            columnName,
            columnType,
            columnSize,
            isPrimaryKey,
            StringUtils.isNotBlank(foreignKeyReference),
            foreignKeyReference
        ));
    }

    /**
     * Returns the column at the specified '1'-based index.
     *
     * @param index
     * @return
     */
    public ColumnMetaData getColumn(int index) {
        return columns.get(index - 1);
    }

    /**
     * Returns the column with the specified name.
     *
     * @param name
     * @return
     */
    public ColumnMetaData getColumn(String name) {
        return columns
            .stream()
            .filter((c) -> StringUtils.equalsIgnoreCase(name, c.columnName))
            .findAny()
            .orElse(null);
    }

    @Override
    public int compareTo(TableMetaData t) {
        return Comparator
            .comparing(TableMetaData::getCanonicalTableName)
            .compare(this, t);
    }

    /**
     * If schemaName is set, returns {schemaName}.{tableName}, otherwise returns
     * {tableName} alone.
     * 
     * @return 
     */
    public String getCanonicalTableName() {
        if (StringUtils.isNotBlank(schemaName)) {
            return schemaName + "." + tableName;
        }
        return tableName;
    }

    @Override
    public String toString() {
        return getCanonicalTableName();
    }
}