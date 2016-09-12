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

package com.strider.datadefender.file.metadata;

import java.util.Comparator;

/**
 * Data object to hold all metadata for matching data in Discovery applications.
 * Currently this object holds column and table metadata; ie, duplicating info.  
 * But since the numbers of tables/columns will always be
 * limited in size, don't really see an immediate need to refactor this.
 * @author Armenak Grigoryan
 */
public class FileMatchMetaData {
    private final String directory;
    private final String fileName;
    private double averageProbability;   
    private String model = "";

    public FileMatchMetaData(final String directory, final String fileName) {
        this.directory = directory;
        this.fileName  = fileName;
    }   

    public String getDirectory() {
        return this.directory;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public void setAverageProbability(final double averageProbability) {
        this.averageProbability = averageProbability;
    }
    
    public double getAverageProbability() {
        return this.averageProbability;
    }
    
    public String getModel() {
        return this.model;
    }

    public void setModel(final String model) {
        this.model = model;
    }        
    
    @Override
    public String toString() {
        return this.directory + "." + this.fileName;
    }
    
    /**
     * @return comparator used for sorting
     */
    public static Comparator<FileMatchMetaData> compare() {
            return Comparator.comparing(FileMatchMetaData::getDirectory)
                              .thenComparing(FileMatchMetaData::getFileName);            
    }
}

