/** 
 * Copyright 2014-2017, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.datadefender.specialcase;

import com.strider.datadefender.database.metadata.MatchMetaData;
import com.strider.datadefender.extensions.BiographicFunctions;
import com.strider.datadefender.utils.CommonUtils;

/**
 *
 * @author strider
 */
public class SinDetector implements SpecialCase {
    
    public static MatchMetaData detectSin(MatchMetaData data, String text) {
        if (CommonUtils.isEmptyString(text)) {
            return null;
        }
        
        if (data.getColumnType().equals("INT") || data.getColumnType().equals("VARCHAR")) {
            final BiographicFunctions bf = new BiographicFunctions();
            if (data.getColumnType().equals("VARCHAR")) {
                text = text.replaceAll("\\D+", "");
            }
            if ( ( text.matches("[0-9]+") && text.length() == 9) && bf.isValidSIN(text)) {
                data.setModel("sin");
                data.setAverageProbability(1);
                return data;
            }
        }
        return null;
    }
}