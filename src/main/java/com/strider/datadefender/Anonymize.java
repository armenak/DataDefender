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
import com.strider.datadefender.requirement.Requirement;

import java.util.concurrent.Callable;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import lombok.extern.log4j.Log4j2;

/**
 * Anonymize picocli subcommand, configures and executes the database
 * anonymizer.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "anonymize",
    version = "2.0",
    description = "Run anonymization utility"
)
@Log4j2
public class Anonymize implements Callable<Integer> {

    @ParentCommand
    private DataDefender dataDefender;

    @Option(names = { "-r", "--requirement-file" }, description = "Requirement XML file", required = true)
    private Requirement requirement;

    @Option(names = { "-b", "--batch-size" }, description = "Number of update queries to batch together", defaultValue = "1000")
    private Integer batchSize;

    @ArgGroup(exclusive = false, multiplicity = "1")
    private DbConfig dbConfig;

    @Parameters(paramLabel = "tables", description = "Limit anonymization to specified tables")
    private List<String> tables;

    @Override
    public Integer call() throws Exception {
        System.out.println("Starting anonymizer");
        log.info("Datasource URL: {}, vendor: {}, schema: {}", dbConfig.getUrl(), dbConfig.getVendor(), dbConfig.getSchema());
        log.info("Username: {}, Password provided: {}", dbConfig.getUsername(), (StringUtils.isNotBlank(dbConfig.getPassword()) ? "yes" : "no"));
        log.info("Batch size: {}", batchSize);
        log.info("Limiting to tables: {}", CollectionUtils.isEmpty(tables) ? "<all tables selected>" : StringUtils.join(tables, ", "));
        System.out.println("");
        // final IAnonymizer anonymizer = new DatabaseAnonymizer();
        return 0;
    }
}
