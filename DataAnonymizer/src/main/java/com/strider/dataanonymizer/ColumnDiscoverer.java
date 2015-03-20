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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

import org.apache.log4j.Logger;

import static org.apache.log4j.Logger.getLogger;

import com.strider.dataanonymizer.database.metadata.ColumnMetaData;
import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.database.metadata.IMetaData;
import com.strider.dataanonymizer.database.metadata.MetaDataFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Armenak Grigoryan
 */
public class ColumnDiscoverer implements IDiscoverer { 
    
    private static final Logger log = getLogger(ColumnDiscoverer.class);

    @Override
    public void discover(Properties databaseProperties, Properties columnProperties, Collection<String> tables) 
    throws DatabaseAnonymizerException {
     
        log.info("Column discovery in process");
        IMetaData metaData = MetaDataFactory.fetchMetaData(databaseProperties);
        List<ColumnMetaData> map = metaData.getMetaData();
        
        // Converting HashMap keys into ArrayList
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<String> suspList = new ArrayList(columnProperties.keySet());
        ArrayList<String> matches = new ArrayList<String>();
        for(String s: suspList) {
            Pattern p = compile(s);
            // Find out if database columns contain any of of the "suspicios" fields
            for(ColumnMetaData pair: map) {
                String tableName = pair.getTableName();
                String columnName = pair.getColumnName();
                if (!tables.isEmpty() && !tables.contains(tableName.toLowerCase())) {
                    continue;
                }
                if (p.matcher(columnName.toLowerCase()).matches()) {
                    matches.add(tableName + "." + columnName);
                }                            
            }            
        }
        
        // Report column names
        log.info("-----------------");        
        log.info("List of suspects:");
        log.info("-----------------");
        Collections.sort(matches);        
        for (String entry: matches) {
            log.info(entry);
        }
    }
}