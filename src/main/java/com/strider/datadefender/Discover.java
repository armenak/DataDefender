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

import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.requirement.file.Generator;
import java.io.File;
import java.util.List;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import lombok.extern.log4j.Log4j2;
import lombok.Getter;
import lombok.Setter;

/**
 * "discover" picocli subcommand, configures and executes the data discoverer.
 *
 * TODO(ZB): Look into setting up command mixins sp dbconfig options don't have
 * to appear before subcommands [https://picocli.info/#_mixins]
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "discover",
    version = "1.0",
    description = "Run data discovery utility",
    mixinStandardHelpOptions = true,
    subcommands = {
        DiscoverColumns.class,
        DiscoverData.class,
        DiscoverFiles.class
    },
    subcommandsRepeatable = true
)
@Log4j2
public class Discover implements Callable<Integer> {

    @Getter
    @Setter
    @ArgGroup(exclusive = false, multiplicity = "0..1", heading = "Database connection settings%n")
    private DbConfig dbConfig;

    @Option(names = { "-o", "--output" }, description = "Generate a requirements xml file and write it out to the specified file")
    @Setter
    private File outputFile;

    @Spec
    private CommandSpec spec;

    public void afterSubcommand() {
        List<IRequirementCommand> subcommands = spec
            .commandLine()
            .getParseResult()
            .subcommands()
            .stream()
            .map((p) -> p.commandSpec().userObject())
            .filter((u) -> (u instanceof IRequirementCommand))
            .map((r) -> (IRequirementCommand) r)
            .collect(Collectors.toList());
        if (subcommands.stream().allMatch((r) -> r.getColumnMetaData() != null)) {
            Set<ColumnMetaData> combined = subcommands.stream().flatMap((r) -> r.getColumnMetaData().stream()).collect(Collectors.toSet());
            try {
                Generator.write(Generator.create(combined), outputFile);
            } catch (Exception e) {
                log.error("Error creating or writing to an output xml file", e);
            }
        }
    }

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
