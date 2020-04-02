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

import com.strider.datadefender.anonymizer.DatabaseAnonymizer;
import com.strider.datadefender.anonymizer.IAnonymizer;
import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.registry.ClassAndFunctionRegistry;

import java.util.concurrent.Callable;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

import lombok.extern.log4j.Log4j2;

/**
 * Anonymize picocli subcommand, configures and executes the database
 * anonymizer.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "anonymize",
    version = "1.0",
    mixinStandardHelpOptions = true,
    description = "Run anonymization utility"
)
@Log4j2
public class Anonymize implements Callable<Integer> {

    @Mixin
    private LogLevelConfig logLevels;

    @Option(names = { "-r", "--requirement-file" }, paramLabel = "<requirementFile>", description = "Requirement XML file", required = true)
    private Requirement requirement;

    @Option(names = { "-b", "--batch-size" }, description = "Number of update queries to batch together", defaultValue = "1000")
    private Integer batchSize;

    @ArgGroup(exclusive = false, multiplicity = "1", heading = "Database connection settings%n")
    private DbConfig dbConfig;

    @Parameters(paramLabel = "tables", description = "Limit anonymization to specified tables")
    private List<String> tables;

    @Override
    public Integer call() throws Exception {
        System.out.println("");
        System.out.println("Starting anonymizer");
        log.info("Datasource URL: {}, vendor: {}, schema: {}", dbConfig.getUrl(), dbConfig.getVendor(), dbConfig.getSchema());
        log.info("Username: {}, Password provided: {}", dbConfig.getUsername(), (StringUtils.isNotBlank(dbConfig.getPassword()) ? "yes" : "no"));
        log.info("Batch size: {}", batchSize);
        log.info("Limiting to tables: {}", CollectionUtils.isEmpty(tables) ? "<all tables selected>" : StringUtils.join(tables, ", "));

        IDbFactory factory = IDbFactory.get(dbConfig);
        ClassAndFunctionRegistry.singleton().initialize(factory);

        final IAnonymizer anonymizer = new DatabaseAnonymizer(
            factory,
            dbConfig,
            batchSize,
            requirement,
            tables
        );
        try {
            anonymizer.anonymize();
        } catch (DataDefenderException e) {
            log.error(e.getMessage());
            log.debug("Exception occurred during anonymization", e);
            return 1;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return 1;
        }
        return 0;
    }
}
