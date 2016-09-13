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

import static org.apache.log4j.Logger.getLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.strider.datadefender.database.DatabaseAnonymizerException;
import com.strider.datadefender.database.IDBFactory;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.Parameter;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.Table;
import com.strider.datadefender.utils.RequirementUtils;

/**
 * Entry point for RDBMS data generator
 *
 * @author Matt Eaton
 */
public class DataGenerator  implements IGenerator {

    private static final Logger log = getLogger(DataGenerator.class);

    /**
     * Generate data to be used by anoymizer.
     * @param dbFactory
     * @param anonymizerProperties  Properties for anonymizer
     * @throws com.strider.datadefender.database.DatabaseAnonymizerException
     */
    @Override
    public void generate(final IDBFactory dbFactory, final Properties anonymizerProperties) throws DatabaseAnonymizerException {
        // Now we collect data from the requirement
        final Requirement requirement = RequirementUtils.load(anonymizerProperties.getProperty("requirement"));
        // Iterate over the requirement and generate data sets
        log.info("Generating data for client " + requirement.getClient() + " Version " + requirement.getVersion());

        for(final Table table : requirement.getTables()) {
            log.info("Table [" + table.getName() + "]. Start ...");

            // Iterate over columns to generate data set for each column
            for (final Column column : table.getColumns()) {
                final Parameter fileParameter = RequirementUtils.getFileParameter(column.getParameters());
                log.info("Column [" + column.getName() + "]. Start...");

                if (fileParameter != null) {
                    log.debug("Processing file " + fileParameter.getValue());
                    
                    // Backup existing data set file
                    if (!backupExistingFile(fileParameter.getValue())) {
                        throw new DatabaseAnonymizerException("Unable to rename existing data set file " + fileParameter.getValue());
                    }
                    
                    final StringBuilder sql = new StringBuilder(100);
                    sql.append("SELECT DISTINCT(").append(column.getName()).append(") FROM ").append(table.getName());
                    try (Statement stmt = dbFactory.getConnection().createStatement();
                        ResultSet rs = stmt.executeQuery(sql.toString());
                        BufferedWriter bw = new BufferedWriter(new FileWriter(fileParameter.getValue()));) {
                        
                        // Write each column value to data set file
                        while (rs.next()) {
                            bw.write(rs.getString(1));
                            bw.newLine();
                        }
                    } catch (IOException ioException) {
                        log.error(ioException.toString());
                        throw new DatabaseAnonymizerException(ioException.toString(), ioException);
                    } catch (SQLException sqle) {
                        log.error(sqle.toString());
                    } 
                }
                log.info("Column [" + column.getName() + "]. End...");
            }
            log.info("Table " + table.getName() + ". End ...");
            log.info("");
        }
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
}