/*
 *
 * Copyright 2014-1018, Armenak Grigoryan, and individual contributors as indicated
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

package com.strider.datadefender;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

import org.apache.log4j.Logger;

import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.database.IDBFactory;
import com.strider.datadefender.database.metadata.IMetaData;
import com.strider.datadefender.database.metadata.MatchMetaData;
import com.strider.datadefender.report.ReportUtil;
import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.utils.Score;

/**
 * @author Armenak Grigoryan
 */
public class ColumnDiscoverer extends Discoverer {
    private static final Logger log = getLogger(ColumnDiscoverer.class);

    public List<MatchMetaData> discover(final IDBFactory factory, 
            final Properties columnProperties, String vendor)
            throws DatabaseDiscoveryException {
        log.info("Column discovery in process");

        final IMetaData           metaData      = factory.fetchMetaData();
        final List<MatchMetaData> map           = metaData.getMetaData(vendor);
        List<MatchMetaData>       uniqueMatches = null;

        // Converting HashMap keys into ArrayList
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final List<String> suspList = new ArrayList(columnProperties.keySet());

        suspList.remove("tables");    // removing 'special' tables property that's not a pattern
        matches = new ArrayList<>();

        for (final String suspStr : suspList) {
            final Pattern p = compile(suspStr);

            // Find out if database columns contain any of of the "suspicious" fields
            for (final MatchMetaData data : map) {
                final String tableName  = data.getTableName();
                final String columnName = data.getColumnName();

                if (p.matcher(columnName.toLowerCase(Locale.ENGLISH)).matches()) {
                    log.debug(data.toVerboseStr());
                    matches.add(data);
                }
            }
        }

        log.info("Preparing report ...");

        // Report column names

        if ((matches != null) &&!matches.isEmpty()) {
            uniqueMatches = new ArrayList<>(new LinkedHashSet<>(matches));
            log.info("-----------------");
            log.info("List of suspects:");
            log.info("-----------------");
            uniqueMatches.sort(MatchMetaData.compare());

            final Score score = new Score();

            for (final MatchMetaData entry : uniqueMatches) {

                // Row count
                final int rowCount = ReportUtil.rowCount(factory, entry.getTableName(), 0);

                // Getting 5 sample values
                final List<String> sampleDataList = ReportUtil.sampleData(factory,
                                                                          entry.getTableName(),
                                                                          entry.getColumnName());

                // Output
                log.info("Column                     : " + entry);
                log.info(CommonUtils.fixedLengthString('=', entry.toString().length() + 30));
                log.info("Number of rows in the table: " + rowCount);
                log.info("Score                      : " + score.columnScore(rowCount));
                log.info("Sample data");
                log.info(CommonUtils.fixedLengthString('-', 11));

                for (final String sampleData : sampleDataList) {
                    log.info(sampleData);
                }

                log.info("");
            }

            log.info("Overall score: " + score.dataStoreScore());
        } else {
            log.info("No suspects have been found. Please refine your criteria.");
        }

        log.info("matches: " + uniqueMatches);

        return uniqueMatches;
    }
}