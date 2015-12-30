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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author armenak
 */
public class SQLToJavaMapping {
    @SuppressWarnings("serial")
    private static final Map<String, String> JAVA_TYPES = new HashMap<String, String>() {{
        put("VARCHAR", STRING);
        put("NVARCHAR", STRING);
        put("VARCHAR2", STRING);
        put("NVARCHAR2", STRING);
        put("LONGNVARCHAR", STRING);
        put("LONGVARCHAR", STRING);
        put("NCHAR", CHAR);
        put("CHAR", CHAR);
    }};
    private static final String STRING = "String";
    private static final String CHAR = "Char";
    
    public static boolean isString(final String type) {
        return STRING.equals(JAVA_TYPES.get(type.toUpperCase())) ||
               CHAR.equals(JAVA_TYPES.get(type.toUpperCase())) ;
    }
}
