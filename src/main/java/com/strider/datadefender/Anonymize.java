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

import java.util.concurrent.Callable;
import java.io.File;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

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
public class Anonymize implements Callable<Integer> {

    @ParentCommand
    private DataDefender dataDefender;

    @Option(names = { "-r", "--requirements-file" }, description = "Requirements XML file", required = true)
    private File requirementsFile;

    @Option(names = { "-b", "--batch-size" }, description = "Number of update queries to batch together", defaultValue = "1000")
    private Integer batchSize;

    @ArgGroup(exclusive = false, multiplicity = "1")
    private DbConfig dbConfig;

    @Override
    public Integer call() throws Exception {
        System.out.println("Starting anonymizer with requirements file: " + requirementsFile.getPath());
        System.out.println("Datasource URL: " + dbConfig.getUrl() + ", vendor: " + dbConfig.getVendor());
        System.out.println("Batch size: " + batchSize);
        System.out.println("");
        // final IAnonymizer anonymizer = new DatabaseAnonymizer();
        return 0;
    }
}
