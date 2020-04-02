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
package com.strider.datadefender.extractor;

import com.strider.datadefender.DbConfig;
import com.strider.datadefender.database.DatabaseException;
import com.strider.datadefender.database.IDbFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

/**
 * Entry point for RDBMS data generator
 *
 * @author Matt Eaton
 */
@Log4j2
public class DataExtractor implements IExtractor {

    final IDbFactory dbFactory;
    final DbConfig config;
    final List<String> tableColumns;

    public DataExtractor(IDbFactory dbFactory, DbConfig config, List<String> tableColumns) {
        this.dbFactory = dbFactory;
        this.config = config;
        this.tableColumns = tableColumns;
    }

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
    public void extract() throws DatabaseException {

        // Iterate over the requirement and generate data sets
        for (String tableColumn : tableColumns) {

            String table = StringUtils.substringBefore(tableColumn, ".");
            String column = StringUtils.substringAfter(tableColumn, ".");
            String fileName = table + "_" + column + ".txt";

            log.info("Table column [{}] into [{}]. Start ...", tableColumn, fileName);

            // Backup existing data set file
            if (!backupExistingFile(fileName)) {
                throw new DatabaseException(
                    "Unable to rename existing data set file "
                    + fileName
                );
            }

            int total = 0;
            String count = "SELECT COUNT(DISTINCT " + column + ") FROM " + table;
            try (Statement stmt = dbFactory.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(count);) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            } catch (SQLException e) {
                throw new DatabaseException(e.toString(), e);
            }

            String query = "SELECT DISTINCT " + column + " FROM " + table;
            try (
                ProgressBar pb = new ProgressBar("Extracting " + tableColumn + "...", total);
                Statement stmt = dbFactory.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(query);
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            ) {
                // Write each column value to data set file
                while (rs.next()) {
                    String col = rs.getString(1);
                    if (StringUtils.isNotBlank(col)) {
                        bw.write(col);
                        bw.newLine();
                    }
                    pb.step();
                }
            } catch (IOException|SQLException e) {
                throw new DatabaseException(e.toString(), e);
            }

            log.info("Table column {}. End ...", tableColumn);
        }
    }
}
