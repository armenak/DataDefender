package com.strider.dataanonymizer.utils;

import java.io.InputStream;
import java.util.Properties;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 *
 * @author Armenak Grigoryan
 */
public final class AppProperties {
    
    static Logger log = Logger.getLogger(AppProperties.class);

    /**
     * Load property file
     * @param String fileName
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
}