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
 */
package com.strider.datadefender;

import com.strider.datadefender.database.metadata.IMetaData;
import com.strider.datadefender.database.metadata.TableMetaData;
import com.strider.datadefender.report.ReportUtil;
import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.utils.Score;
import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import lombok.extern.log4j.Log4j2;

import static java.util.regex.Pattern.compile;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class ColumnDiscoverer extends Discoverer {

    public List<ColumnMetaData> discover(final IDbFactory factory,
            final Properties columnProperties, String vendor)
            throws DataDefenderException, IOException, SQLException {
        log.info("Column discovery in process");

        final IMetaData metaData = factory.fetchMetaData();
        final List<TableMetaData> list = metaData.getMetaData();

        // Converting HashMap keys into ArrayList
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final List<String> suspList = new ArrayList(columnProperties.keySet());
        suspList.remove("tables");    // removing 'special' tables property that's not a pattern

        final List<Pattern> patterns = suspList.stream().map((s) -> compile(s)).collect(Collectors.toList());
        List<ColumnMetaData> columns = list.stream().flatMap((t) -> t.getColumns().stream())
            .filter((c) -> patterns.stream().anyMatch((p) -> p.matcher(c.getColumnName().toLowerCase()).matches()))
            .collect(Collectors.toList());

        log.info("Preparing report ...");

        // Report column names
        List<ColumnMetaData> uniqueMatches = null;
        if (CollectionUtils.isNotEmpty(columns)) {
            uniqueMatches = new ArrayList<>(new LinkedHashSet<>(columns));
            log.info("-----------------");
            log.info("List of suspects:");
            log.info("-----------------");
            uniqueMatches.sort((a, b) -> a.compareTo(b));

            final Score score = new Score();

            for (final ColumnMetaData entry : uniqueMatches) {

                // Row count
                final int rowCount = ReportUtil.rowCount(factory, entry.getTable().getTableName());

                // Getting 5 sample values
                final List<String> sampleDataList = ReportUtil.sampleData(factory, entry);

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

        return uniqueMatches;
    }
}