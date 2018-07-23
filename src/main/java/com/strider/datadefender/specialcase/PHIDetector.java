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
 *
 */

package com.strider.datadefender.specialcase;

import com.strider.datadefender.database.metadata.MatchMetaData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.functions.CoreFunctions;
import com.strider.datadefender.utils.CommonUtils;

/**
 * @author Armenak Grigoryan
 */
public class PHIDetector extends CoreFunctions {
        
    private static final Logger log = getLogger(PHIDetector.class);

    private static List phiList = new ArrayList();
    
    static {
        String file = "phi.txt";
        try {
            log.info("*** reading from " + file);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line; (line = br.readLine()) != null; ) {
                    phiList.add(line);
                }
            }
        } catch (IOException ioe) {
            log.error(ioe.toString());
        }
    }
    
    /**
     * Generates random 9-digit student number 
     * @param data
     * @param text
     * @return String
     */
    public static MatchMetaData isPHITerm(final MatchMetaData data, String text) {    
       if (CommonUtils.isEmptyString(text)) {
            return null;
        }
        
        if (data.getColumnType().equals("VARCHAR")) {
            if (phiList.contains(text.trim().toUpperCase())) {
                data.setModel("phi");
                data.setAverageProbability(1);
                return data;
            }
        }
        return null;
    }
}