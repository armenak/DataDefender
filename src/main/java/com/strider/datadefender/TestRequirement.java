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
import picocli.CommandLine.Parameters;

import lombok.extern.log4j.Log4j2;

/**
 * test-requirement cli subcommand for testing the requirements file.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "test-requirement",
    version = "1.0",
    mixinStandardHelpOptions = true,
    description = "Loads the requirement file without attempting to anonymize or process anything to check for syntax issues"
)
@Log4j2
public class TestRequirement implements Callable<Integer> {

    @Option(names = { "-r", "--requirement-file" }, paramLabel = "<requirementFile>", description = "Requirement XML file", required = true)
    private Requirement requirement;

    @Override
    public Integer call() throws Exception {
        System.out.println("");
        System.out.println("Requirements file loaded successfully");
        return 0;
    }
}
