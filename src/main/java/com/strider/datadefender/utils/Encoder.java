/*
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
 */
package com.strider.datadefender.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * This class implements determenistic data anonymization 
 * by encoding the data using "salt" and decoding it.
 * 
 * @author Armenak Grigoryan
 */
@Log4j2
public final class Encoder {
    
    private static final String[][] nameArr = {
            {"A", "B"},
            {"B", "C"},
            {"C", "D"},            
            {"D", "E"},                        
            {"E", "F"},                                    
            {"F", "G"},  
            {"G", "H"},                                    
            {"H", "I"},                                    
            {"I", "J"},                                    
            {"J", "K"},                                    
            {"K", "L"},                                    
            {"L", "M"},                                    
            {"M", "N"},                                    
            {"N", "O"},                                    
            {"O", "P"},                                    
            {"P", "Q"},                                    
            {"Q", "R"},                                    
            {"R", "S"},                                    
            {"S", "T"},                                    
            {"T", "U"},                                    
            {"U", "V"},                                    
            {"V", "W"},                                    
            {"W", "X"},                                    
            {"X", "Y"},                                    
            {"Y", "Z"},                                    
            {"Z", "A"}                                    
        };
        
    
    public static String encode(String originalValue) {

        char[] cData = originalValue.toCharArray();

        StringBuilder newValue = new StringBuilder();
        for (char c: cData) {
            for (int i = 0; i<nameArr.length; i++) {
                String nameChar = Character.toString(c);
                String nameArrChar = nameArr[i][0];
                if (nameChar.toLowerCase().equals(nameArrChar.toLowerCase())) {
                    if (StringUtils.isAllLowerCase(nameChar)) {
                        newValue.append(nameArr[i][1].toLowerCase());
                    } else {
                        newValue.append(nameArr[i][1]);
                    }
                }
            }
        }

        return newValue.toString();
    }
    
    public static String decode(String encodedValue) {

        char[] cData = encodedValue.toCharArray();
        
        StringBuilder newValue = new StringBuilder();
        for (char c: cData) {
            for (int i = 0; i<nameArr.length; i++) {
                String nameChar = Character.toString(c);
                String nameArrChar = nameArr[i][1];
                if (nameChar.toLowerCase().equals(nameArrChar.toLowerCase())) {
                    if (StringUtils.isAllLowerCase(nameChar)) {
                        newValue.append(nameArr[i][0].toLowerCase());
                    } else {
                        newValue.append(nameArr[i][0]);
                    }
                }
            }
        }

        return newValue.toString();
    }    
    
}
