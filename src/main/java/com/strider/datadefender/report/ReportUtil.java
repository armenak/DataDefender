/*
 *
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
 *
 */



package com.strider.datadefender.report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.database.metadata.TableMetaData;
import com.strider.datadefender.database.sqlbuilder.ISQLBuilder;
import com.strider.datadefender.utils.CommonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.util.HashSet;
import org.apache.commons.io.IOUtils;
import com.strider.datadefender.database.IDbFactory;

/**
 *
 * @author Armenak Grigoryan
 */
public class ReportUtil {
    private static final Logger log = getLogger(ReportUtil.class);

    public static int rowCount(final IDbFactory factory, final String tableName) {
        final ISQLBuilder sqlBuilder = factory.createSQLBuilder();
        final String      table      = sqlBuilder.prefixSchema(tableName);
        
        // Getting number of records in the table
        final String queryCount = "SELECT count(*) " + " FROM " + table;
        log.debug("Executing query against database: " + queryCount);

        int rowCount = 0;

        try (Statement stmt = factory.getConnection().createStatement();
            ResultSet resultSet = stmt.executeQuery(queryCount);) {
            resultSet.next();
            rowCount = resultSet.getInt(1);
        } catch (SQLException sqle) {
            log.error(sqle.toString());
        }

        return rowCount;
    }

    public static List<String> sampleData(final IDbFactory factory, final TableMetaData metaData) throws IOException {
        final ISQLBuilder sqlBuilder  = factory.createSQLBuilder();
        String            querySample = "";
        String            select      = "SELECT ";
        
        if (!metaData.getColumnType().equals("CLOB")) {
            select = select + "DISTINCT ";
        }
        
        querySample = sqlBuilder.buildSelectWithLimit(select + metaData.getColumnName() + 
                                                          " FROM "  + sqlBuilder.prefixSchema(metaData.getTableName()) +
                                                          " WHERE " + metaData.getColumnName() + " IS NOT NULL",
                                                          5);     
        log.debug("Executing query against database: " + querySample);

        final List<String> sampleDataList = new ArrayList<>();

        try (Statement stmt = factory.getConnection().createStatement();
            ResultSet resultSet = stmt.executeQuery(querySample);) {
            while (resultSet.next()) {
                String tmp;
                if (metaData.getColumnType().equals("CLOB")) {
                    Clob clob = resultSet.getClob(1);
                    InputStream is = clob.getAsciiStream();
                    tmp = IOUtils.toString(is, StandardCharsets.UTF_8.name());
                } else {
                    tmp = resultSet.getString(1);
                }
                
                if (!CommonUtils.isEmptyString(tmp) && !tmp.equals(" ")) {
                    sampleDataList.add(tmp);
                    tmp = "";
                }
            }
        } catch (SQLException sqle) {
            log.error(sqle.toString());
        }

        // Removing duplicates
        List<String> sampleDataListWithoutDuplicates = 
                new ArrayList<>(new HashSet<>(sampleDataList));
        
        return sampleDataListWithoutDuplicates;
    }
}
