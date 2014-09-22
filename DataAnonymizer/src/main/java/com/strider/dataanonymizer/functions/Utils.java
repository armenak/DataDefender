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

package com.strider.dataanonymizer.functions;

import static java.lang.Integer.parseInt;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author Armenak Grigoryan
 */
public class Utils {
    
    /** 
     * The string used to separator package and class name 
     */
    public static final String SEPARATOR = ".";
    
    /**
     * Initializes logger
     */
    private static Logger log = getLogger(Utils.class);

    
    /**
     * Returns the method name.
     * 
     * @param fullMethodName Fully specified method name.
     * @return Method name.
     * 
     * @throws RuntimeException Parameter is an empty string.
     */
    public static String getMethodName(final String fullMethodName) {
        if (fullMethodName.length() == 0) {
            throw new RuntimeException("Please specify fully specified methid name in Requirement document");
        }

        int index = fullMethodName.lastIndexOf(SEPARATOR);
        if (index != -1) {
            return fullMethodName.substring(index+1);
        }
        
        return "";    
    }
    
    /**
     * Returns fully specified class name.
     * 
     * @param fullMethodName Fully specified method name.
     * @return Method name.
     * 
     * @throws RuntimeException Parameter is an empty string.
     */
    public static String getClassName(final String fullMethodName) {
        if (fullMethodName.length() == 0) {
            throw new RuntimeException("Please specify fully specified methid name in Requirement document");
        }

        int index = fullMethodName.lastIndexOf(SEPARATOR);
        if (index != -1) {
            return fullMethodName.substring(0,index);
        }
        
        return "";    
    }
    
    /**
     * Determines whether the string represents integer
     * 
     * @param str String that should represent integer
     * @return true if the string is integer, false otherwise
     */
    public static boolean isInteger(String str) {
        try {
            parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            log.error(nfe.toString());
        }
        
        return false;
    }    
}