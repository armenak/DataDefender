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

package com.strider.datadefender.utils;

import static org.apache.log4j.Logger.getLogger;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.strider.datadefender.DatabaseDiscoveryException;

/**
 *
 * @author Armenak Grigoryan
 */
public final class AppProperties {
    
    private static final Logger log = getLogger(AppProperties.class);

    /**
     * Load property file
     * @param fileName
     * @return Properties
     */
    public static Properties loadPropertiesFromClassPath(final String fileName) {
        final Properties props = new Properties();
        InputStream input = null;
 
    	try {
            input = AppProperties.class.getClassLoader().getResourceAsStream(fileName);
            if(input==null){
    	        log.warn("Unable to find " + fileName);
                return null;
            }
            //load a properties file from class path, inside static method
            props.load(input);
    	} catch (IOException ex) {
            log.error(ex.toString());
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    log.error(e.toString());
                }
            }
        }
 
        return props;
    }
 
    public static Properties loadProperties(final String fileName) throws DatabaseDiscoveryException {
        final Properties properties = new Properties();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(fileName), "UTF-8")) {
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new DatabaseDiscoveryException("ERROR: Unable to load " + fileName, e);
        }
    }
}