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
import com.strider.datadefender.utils.CommonUtils;

/**
 * @author Armenak Grigoryan
 */
public class SinDetector implements SpecialCase {
    private static final Logger LOG = getLogger(SinDetector.class);

    public static MatchMetaData detectSin(final MatchMetaData data, final String text) {
        String sinValue = "";
        
        if (!CommonUtils.isEmptyString(text)) {
            sinValue = text;
        }        
        
        if ((data.getColumnType().equals("INT") || data.getColumnType().equals("VARCHAR")
                    || data.getColumnType().equals("CHAR"))) {

            if (data.getColumnType().equals("VARCHAR")) {
                sinValue = sinValue.replaceAll("\\D+", "");
            }

            if (isValidSIN(sinValue)) {
                LOG.info("SIN detected: " + sinValue + " in " + data.getTableName() + "." + data.getColumnName());
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
    
    /**
     * Algorithm is taken from https://en.wikipedia.org/wiki/Social_Insurance_Number
     * @param sin
     * @return boolean true, if SIN is valid, otherwise false
     */
    private static boolean isValidSIN(final String sin) {

        if ((sin.length() != 9)) {
            LOG.debug("SIN length is != 9");
            return false;
        }

        if (!sin.matches("[0-9]+")) {
            LOG.debug("SIN " + sin + " is not number");
            return false;
        }

        final int[]         sinArray   = new int[sin.length()];
        final int[]         checkArray = {
            1, 2, 1, 2, 1, 2, 1, 2, 1
        };
        final List<Integer> sinList    = new ArrayList();
        for (int i = 0; i < 9; i++) {
            sinArray[i] = Integer.valueOf(sin.substring(i, i + 1));
            sinArray[i] = sinArray[i] * checkArray[i];
        }

        int sum = 0;

        for (int i = 0; i < 9; i++) {
            final String tmp = String.valueOf(sinArray[i]);

            if (tmp.length() == 1) {
                sinList.add(Integer.valueOf(tmp));
                sum += Integer.valueOf(tmp);
            } else {
                sinList.add(Integer.valueOf(tmp.substring(0, 1)));
                sum += Integer.valueOf(tmp.substring(0, 1));
                sinList.add(Integer.valueOf(tmp.substring(1, 2)));
                sum += Integer.valueOf(tmp.substring(1, 2));
            }
        }

        if ((sum % 10) == 0) {
            return true;
        }
        
        return false;
    }    
}