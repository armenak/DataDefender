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



package com.strider.datadefender.database.metadata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.strider.datadefender.Probability;

/**
 * Data object to hold all metadata for matching data in Discovery applications.
 * Currently this object holds column and table metadata; ie, duplicating info.
 * But since the numbers of tables/columns will always be
 * limited in size, don't really see an immediate need to refactor this.
 * @author Armenak Grigoryan
 */
public class MatchMetaData {
    private String             model           = "";
    private List<Probability>  probabilityList = new ArrayList<>();
    private final String       schemaName;
    private final String       tableName;
    private final List<String> pKeys;
    private final List<String> fKeys;    
    private final String       columnName;
    private final String       columnType;
    private final int          columnSize;
    private double             averageProbability;

    public MatchMetaData(final String schemaName, final String tableName, final List<String> pKeys,
                         final List<String> fKeys, final String columnName, final String columnType, 
                         final int columnSize) {
        this.schemaName = schemaName;
        this.tableName  = tableName;
        this.pKeys      = pKeys;
        this.fKeys      = fKeys;        
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnSize = columnSize;
    }

    /**
     * @return comparator used for sorting
     */
    public static Comparator<MatchMetaData> compare() {
        return Comparator.comparing(MatchMetaData::getSchemaName)
                         .thenComparing(MatchMetaData::getTableName)
                         .thenComparing(MatchMetaData::getColumnName);
    }

    @Override
    public String toString() {
        return this.tableName + "." + this.columnName;
    }

    public String toVerboseStr() {
        return this.schemaName + "." + toString() + "(" + this.columnType + ")";
    }

    public double getAverageProbability() {
        return this.averageProbability;
    }

    public void setAverageProbability(final double averageProbability) {
        this.averageProbability = averageProbability;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public int getColumnSize() {
        return this.columnSize;
    }

    public String getColumnType() {
        return this.columnType;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public List<String> getPkeys() {
        return pKeys;
    }
    
    public List<String> getFkeys() {
        return fKeys;
    }    

    public List<Probability> getProbabilityList() {
        return this.probabilityList;
    }

    public void setProbabilityList(final List<Probability> probabilityList) {
        this.probabilityList = probabilityList;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public String getTableName() {
        return this.tableName;
    }
}