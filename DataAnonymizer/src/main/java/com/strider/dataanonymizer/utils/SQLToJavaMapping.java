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
    
    private static final Map<Integer, String> SQL_TO_JAVA = new HashMap();
    
    static {
        SQL_TO_JAVA.put(java.sql.Types.VARCHAR, "String");
        SQL_TO_JAVA.put(java.sql.Types.NVARCHAR, "String");
        SQL_TO_JAVA.put(java.sql.Types.NCHAR, "Char");
        SQL_TO_JAVA.put(java.sql.Types.CHAR, "Char");
    }
    
    public static boolean isString(final int type) {
        final String value = SQL_TO_JAVA.get(type);
        if ( (value != null) && (value.equals("String"))) {
            return true;
        }
        return false;
    }
    
}
