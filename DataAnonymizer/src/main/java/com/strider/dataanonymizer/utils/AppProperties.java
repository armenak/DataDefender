package com.strider.dataanonymizer.utils;

import java.io.InputStream;
import java.util.Properties;
import java.io.IOException;

/**
 *
 * @author Armenak Grigoryan
 */
public final class AppProperties {
    public static Properties loadPropertiesFromClassPath(String fileName) {
        Properties props = new Properties();
        InputStream input = null;
 
    	try {
    		input = AppProperties.class.getClassLoader().getResourceAsStream(fileName);
    		if(input==null){
    	            System.out.println("Sorry, unable to find " + fileName);
    		    return null;
    		}
 
    		//load a properties file from class path, inside static method
    		props.load(input);
 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        } finally{
            if(input!=null){
                    try {
                            input.close();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
            }
        }
 
        return props;
    }
}
