/** 
 * Copyright 2014-2018, Armenak Grigoryan, and individual contributors as indicated
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.Probability;
import com.strider.datadefender.database.metadata.MatchMetaData;
import com.strider.datadefender.extensions.BiographicFunctions;
import com.strider.datadefender.utils.CommonUtils;

/**
 * @author Armenak Grigoryan
 */
public class SinDetector implements SpecialCase {
    
    private static final Logger log = getLogger(SinDetector.class);
    
    public static MatchMetaData detectSin(final MatchMetaData data, final String text) {
        String sinValue = text;
        
        if (CommonUtils.isEmptyString(sinValue)) {
            return null;
        }
        
        if (data.getColumnType().equals("INT") || data.getColumnType().equals("VARCHAR") || data.getColumnType().equals("CHAR")) {
            final BiographicFunctions bf = new BiographicFunctions();
            if (data.getColumnType().equals("VARCHAR")) {
                sinValue = sinValue.replaceAll("\\D+", "");
            }
            if ( bf.isValidSIN(sinValue)) {
                log.info("SIN detected: " + sinValue + " in " + data.getTableName() + "." + data.getColumnName());
                data.setModel("sin");
                data.setAverageProbability(1);
                final List<Probability> probabilityList = new ArrayList();
                probabilityList.add(new Probability(sinValue, 1.00));
                data.setProbabilityList(probabilityList);                
                return data;
            }
        }
        return null;
    }
}