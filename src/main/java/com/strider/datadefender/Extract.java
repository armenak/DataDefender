/*
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
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

import com.strider.datadefender.extractor.IExtractor;
import com.strider.datadefender.extractor.DataExtractor;
import com.strider.datadefender.database.IDbFactory;

import java.util.concurrent.Callable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import lombok.extern.log4j.Log4j2;

/**
 * "extract" picocli subcommand, configures and executes the data extractor.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "extract",
    version = "2.0",
    mixinStandardHelpOptions = true,
    description = "Run data extraction utility -- generates files out of table "
        + "columns with the name 'table_columnName.txt' for each column "
        + "requested."
)
@Log4j2
public class Extract implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @ArgGroup(exclusive = false, multiplicity = "1")
    private DbConfig dbConfig;

    private List<String> tableColumns;

    @Parameters(paramLabel = "columns", description = "Generate data for the specified table.columName(s)")
    public void setTableColumns(List<String> tableColumns) {
        for (String tableColumn : tableColumns) {
            int loc = tableColumn.indexOf(".");
            if (loc < 1 || loc >= tableColumn.length() - 1) {
                throw new ParameterException(
                    spec.commandLine(),
                    String.format(
                        "Columns must be specified in the form [table].[columnName], found: %s",
                        tableColumn
                    )
                );
            }
        }
        this.tableColumns = tableColumns;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("");
        System.out.println("Starting data extractor");
        log.info("Datasource URL: {}, vendor: {}, schema: {}", dbConfig.getUrl(), dbConfig.getVendor(), dbConfig.getSchema());
        log.info("Username: {}, Password provided: {}", dbConfig.getUsername(), (StringUtils.isNotBlank(dbConfig.getPassword()) ? "yes" : "no"));
        log.info("Extracting data from: {}", tableColumns);

        IDbFactory factory = IDbFactory.get(dbConfig);

        final IExtractor extractor = new DataExtractor(
            factory,
            dbConfig,
            tableColumns
        );
        try {
            extractor.extract();
        } catch (DataDefenderException e) {
            log.error(e.getMessage());
            log.debug("Exception occurred during data extraction", e);
            return 1;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return 1;
        }
        return 0;
    }
}
