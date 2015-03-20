/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.dataanonymizer.utils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author armenak
 */
public class SQLToJavaMapping {
    private static final Map<String, String> JAVA_TYPES = new HashMap<String, String>();

    static { 
        JAVA_TYPES.put("VARCHAR", "String");
        JAVA_TYPES.put("NVARCHAR", "String");
        JAVA_TYPES.put("VARCHAR2", "String");
        JAVA_TYPES.put("NVARCHAR2", "String");
        JAVA_TYPES.put("LONGNVARCHAR", "String");
        JAVA_TYPES.put("LONGVARCHAR", "String");
        JAVA_TYPES.put("NCHAR", "Char");        
        JAVA_TYPES.put("CHAR", "Char");
    }
    
    public static boolean isString(final String type) {
        final String value = JAVA_TYPES.get(type);
        if ( (value != null) && (value.equals("String"))) {
            return true;
        }
        return false;
    }    
}
