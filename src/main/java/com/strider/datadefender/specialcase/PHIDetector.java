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


import com.strider.datadefender.Probability;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.database.metadata.MatchMetaData;
import java.util.Locale;

/**
 * @author Armenak Grigoryan
 */
public class PHIDetector implements SpecialCase {
        
    private static final Logger log = getLogger(PHIDetector.class);
    private static final String PHI_FILE = "phi.txt";
    private static List phiList = new ArrayList();
    
    static {
        try {
            log.info("*** reading from " + PHI_FILE);
            try (BufferedReader br = new BufferedReader(new FileReader(PHI_FILE))) {
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
    public static MatchMetaData isPHITerm(final MatchMetaData data, final String text) {   
       if (CommonUtils.isEmptyString(text)) {
            return null;
        }
        
        if (data.getColumnType().equals("VARCHAR") &&
            phiList.contains(text.trim().toLowerCase(Locale.ENGLISH))) {
                log.info("PHI detected: " + text);
                data.setModel("phi");
                data.setAverageProbability(100);
                List<Probability> probabilityList = new ArrayList();
                probabilityList.add(new Probability(text, 1.00));
                data.setProbabilityList(probabilityList);
                return data;
        }
        return null;
    }
}