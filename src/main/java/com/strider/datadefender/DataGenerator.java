/*
 *
 * Copyright 2014, Armenak Grigoryan, Matthew Eaton, and individual contributors as indicated
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

import com.strider.datadefender.database.DatabaseException;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.Argument;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.Table;
import com.strider.datadefender.requirement.file.Generator;
import com.strider.datadefender.database.IDbFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Entry point for RDBMS data generator
 *
 * @author Matt Eaton
 */
@Log4j2
public class DataGenerator implements IGenerator {

    /**
     * Rename current data set file to .bak-N, where N is an incremented integer
     * @param file Path and name of file to rename
     * @return true if renamed successfully, false otherwise
     */
    private boolean backupExistingFile(final String file) {

        final File sourceFile = new File(file);

        if (!sourceFile.exists()) {
            return true;
        }

        int count = 1;
        while (new File(file + ".bak-" + count).exists()) {
            count++;
        }
        return sourceFile.renameTo(new File(file + ".bak-" + count));
    }

    /**
     * Generate data to be used by anoymizer.
     * @param dbFactory
     * @param Requirement requirement file
     * @throws com.strider.datadefender.database.DatabaseException
     */
    @Override
    public void generate(final IDbFactory dbFactory, final Requirement requirement)
        throws DatabaseException {

        // Iterate over the requirement and generate data sets
        log.info("Generating data for Client: {} Version: {}", requirement.getClient(), requirement.getVersion());

        for (final Table table : requirement.getTables()) {
            log.info("Table [{}]. Start ...", table.getName());

            // Iterate over columns to generate data set for each column
            for (final Column column : table.getColumns()) {
                final Argument fileParameter = Generator.getFileParameter(
                    column.getFunctionList().getFunctions().stream().flatMap((fn) -> fn.getArguments().stream()).collect(Collectors.toList())
                );

                log.info("Column [{}]. Start...", column.getName());

                if (fileParameter != null) {
                    log.debug("Processing file {}", fileParameter.getValueAttribute());

                    // Backup existing data set file
                    if (!backupExistingFile(fileParameter.getValueAttribute())) {
                        throw new DatabaseException(
                            "Unable to rename existing data set file "
                            + fileParameter.getValueAttribute()
                        );
                    }

                    final StringBuilder sql = new StringBuilder(100);
                    sql.append("SELECT DISTINCT(").append(column.getName()).append(") FROM ").append(table.getName());
                    if (!StringUtils.isBlank(table.getWhere())) {
                        log.info("Where [{}]", table.getWhere());
                        sql.append(" WHERE ").append(table.getWhere());
                    }

                    try (Statement stmt = dbFactory.getConnection().createStatement();
                        ResultSet rs = stmt.executeQuery(sql.toString());
                        BufferedWriter bw = new BufferedWriter(new FileWriter(fileParameter.getValueAttribute()));) {

                        // Write each column value to data set file
                        while (rs.next()) {
                            bw.write(rs.getString(1));
                            bw.newLine();
                        }
                    } catch (IOException|SQLException e) {
                        throw new DatabaseException(e.toString(), e);
                    }
                }

                log.info("Column [{}]. End...", column.getName());
            }

            log.info("Table {}. End ...", table.getName());
        }
    }
}
