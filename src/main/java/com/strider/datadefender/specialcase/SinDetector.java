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
 */
package com.strider.datadefender.specialcase;

import com.strider.datadefender.Discoverer.ColumnMatch;
import com.strider.datadefender.Probability;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.extensions.BiographicFunctions;
import com.strider.datadefender.file.metadata.FileMatchMetaData;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class SinDetector implements SpecialCase {

    public static ColumnMatch detectSin(final ColumnMetaData data, final String text) {
        String sinValue = text;

        if (
            StringUtils.isNotBlank(sinValue)
            && (
                Objects.equals(String.class, data.getColumnType())
                || Number.class.isAssignableFrom(data.getColumnType())
            )
        ) {
            final BiographicFunctions bf = new BiographicFunctions();

            if (Objects.equals(String.class, data.getColumnType())) {
                sinValue = sinValue.replaceAll("\\D+", "");
            }

            if (bf.isValidSIN(sinValue)) {
                log.info("SIN detected: " + sinValue + " in " + data.getTable().getTableName() + "." + data.getColumnName());
                return new ColumnMatch(
                    data,
                    1,
                    "sin",
                    List.of(new Probability(sinValue, 1.00))
                );
            }
        }

        return null;
    }

    public static FileMatchMetaData detectSin(final FileMatchMetaData metaData, final String text) {
        String sinValue = "";

        if (StringUtils.isNotBlank(text)) {
            sinValue = text;
        }

        log.debug("Trying to find SIN in file " + metaData.getFileName() + " : " + sinValue);
        final BiographicFunctions bf = new BiographicFunctions();
        if (bf.isValidSIN(sinValue)) {
                log.info("SIN detected: " + sinValue);
                metaData.setAverageProbability(1.0);
                metaData.setModel("sin");
                return metaData;
        } else {
            log.debug("SIN " + sinValue + " is not valid" );
        }

        return null;
    }
}
