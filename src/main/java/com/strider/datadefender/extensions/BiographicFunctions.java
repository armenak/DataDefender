/*
 * Copyright 2014-2016, Armenak Grigoryan, Matthew Eaton, and individual contributors as indicated
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
package com.strider.datadefender.extensions;

import com.strider.datadefender.anonymizer.functions.Core;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Matthew Eaton
 */
@Log4j2
public class BiographicFunctions extends Core {

    /**
     * Algorithm is taken from https://en.wikipedia.org/wiki/Social_Insurance_Number
     * @param sin
     * @return boolean true, if SIN is valid, otherwise false
     */
    public static boolean isValidSIN(final String sin) {
        boolean valid = false;

        if (sin.length() < 9) {
            log.debug("SIN length is < 9");

            return valid;
        }

        if (!StringUtils.isNumeric(sin)) {
            log.debug("SIN " + sin + " is not number");

            return valid;
        }

        final int[]         sinArray   = new int[sin.length()];
        final int[]         checkArray = {
            1, 2, 1, 2, 1, 2, 1, 2, 1
        };
        final List<Integer> sinList    = new ArrayList();

        log.info(sin);

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
            valid = true;
        }

        return valid;
    }
}
