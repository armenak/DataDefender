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

package com.strider.dataanonymizer.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author Armenak Grigoryan
 */
public final class AppProperties {
    
    private static Logger log = getLogger(AppProperties.class);

    /**
     * Load property file
     * @param fileName
     * @return Properties
     */
    public static Properties loadPropertiesFromClassPath(String fileName) {
        Properties props = new Properties();
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
 
    public static Properties loadProperties(String fileName) throws IOException {
        
        Properties properties = new Properties();
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            properties.load(in);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ex) {
                    log.error(ex.toString(), ex);
                }
            }
        }
        return properties;
    }
}