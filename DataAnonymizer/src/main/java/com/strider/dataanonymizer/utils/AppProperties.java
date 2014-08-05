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