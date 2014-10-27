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
    
    private static final Map<Integer, String> sqlToJava = new HashMap();
    
    static {
        sqlToJava.put(java.sql.Types.VARCHAR, "String");
        sqlToJava.put(java.sql.Types.NVARCHAR, "String");
        sqlToJava.put(java.sql.Types.NCHAR, "Char");
        sqlToJava.put(java.sql.Types.CHAR, "Char");
    }
    
    public static boolean isString(final int type) {
        final String value = sqlToJava.get(type);
        if ( (value != null) && (value.equals("String"))) {
            return true;
        }
        return false;
    }
    
}
