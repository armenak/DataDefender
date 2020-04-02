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

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.database.sqlbuilder.ISqlBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Armenak Grigoryan
 */
@Log4j2
public class ReportUtil {
    
    public static int rowCount(final IDbFactory factory, final String tableName) throws DataDefenderException {
        final ISqlBuilder sqlBuilder = factory.createSQLBuilder();
        final String      table      = sqlBuilder.prefixSchema(tableName);
        
        // Getting number of records in the table
        final String queryCount = "SELECT count(*) FROM " + table;
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

    public static List<String> sampleData(final IDbFactory factory, final ColumnMetaData metaData) throws IOException, DataDefenderException {
        final ISqlBuilder sqlBuilder  = factory.createSQLBuilder();
        String            querySample = "";
        String            select      = "SELECT ";
        
        
        
        if (!metaData.getColumnType().equals("CLOB") && !factory.getVendorName().equals("mssql")) {
            select = select + "DISTINCT ";
        }
        
        querySample = sqlBuilder.buildSelectWithLimit(
            select + metaData.getColumnName() + " FROM "
                + sqlBuilder.prefixSchema(metaData.getTable().getTableName())
                + " WHERE " + metaData.getColumnName() + " IS NOT NULL",
            5
        );
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
                
                if (StringUtils.isNotBlank(tmp)) {
                    sampleDataList.add(tmp);
                    tmp = null;
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
