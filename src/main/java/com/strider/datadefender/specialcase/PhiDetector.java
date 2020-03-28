/*
 * Copyright 2014-2020, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.datadefender.specialcase;

import com.strider.datadefender.discoverer.Discoverer.ColumnMatch;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.strider.datadefender.discoverer.Probability;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import java.util.Objects;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class PhiDetector implements SpecialCase {

    private static final String PHI_FILE = "phi.txt";
    private static List         phiList  = new ArrayList();

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
    public static ColumnMatch isPhiTerm(final ColumnMetaData data, final String text) {
        if (StringUtils.isNotBlank(text)
            && Objects.equals(String.class, data.getColumnType())
            && phiList.contains(text.trim().toLowerCase(Locale.ENGLISH))) {
            
            log.debug("PHI detected: " + text);
            return new ColumnMatch(
                data,
                100.0,
                "phi",
                List.of(new Probability(text, 1.00))
            );
        }
        return null;
    }
}