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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;

import static org.apache.commons.collections.IteratorUtils.toList;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import com.strider.dataanonymizer.database.metadata.ColumnMetaData;
import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.database.metadata.IMetaData;
import com.strider.dataanonymizer.database.metadata.MetaDataFactory;

/**
 * @author Armenak Grigoryan
 */
public class ColumnDiscoverer implements IDiscoverer { 
    
    private static final Logger log = getLogger(ColumnDiscoverer.class);

    @Override
    public void discover(Properties databaseProperties, Properties columnProperties) 
    throws DatabaseAnonymizerException {
        
        IMetaData metaData = MetaDataFactory.fetchMetaData(databaseProperties);
        List<ColumnMetaData> map = metaData.getMetaData();
        
        // Get the list of "suspicios" field names from property file
//        // Reading configuration file
//        Configuration columnsConfiguration = null;
//        try {
//            columnsConfiguration = new PropertiesConfiguration("columndiscovery.properties");
//        } catch (ConfigurationException ex) {
//            log.error(ex.toString());
//        }        
//        Iterator<String> iterator = columnsConfiguration.getKeys();
//        List<String> suspList = toList(iterator);
        
	// Converting HashMap keys into ArrayList
        List<String> suspList = new ArrayList(columnProperties.keySet());
        System.out.println("\n==> Size of Key list: " + suspList.size());
 
        ArrayList<String> matches = new ArrayList<>();
        for(String s: suspList) {
            Pattern p = compile(s);
            // Find out if database columns contain any of of the "suspicios" fields
            for(ColumnMetaData pair: map) {
                String tableName = pair.getTableName();
                String columnName = pair.getColumnName();
                if (p.matcher(columnName).matches()) {
                    matches.add(tableName + "." + columnName);
                }                            
            }            
        }
        
        // Report column names
        log.info("-----------------");        
        log.info("List of suspects:");
        log.info("-----------------");
        for (String entry: matches) {
            log.info(entry);
        }
    }
}