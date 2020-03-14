/*
 * Copyright 2014-2018, Armenak Grigoryan, and individual contributors as indicated
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

import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.utils.RequirementUtils;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

import lombok.extern.log4j.Log4j2;

/**
 * Entry point to Data Defender.
 *
 * This class will parse and analyze the parameters and execute appropriate
 * service.
 *
 */
@Command(
    name = "datadefender",
    mixinStandardHelpOptions = true,
    version = "2.0",
    description = "Data detection and anonymization tool",
    synopsisSubcommandLabel = "COMMAND",
    subcommands = {
        HelpCommand.class,
        //FileDiscoverer.class,
        Anonymize.class,
        //DataGenerator.class,
        //ColumnDiscoverer.class
    }
)
@Log4j2
public class DataDefender implements Callable<Integer> {

    public static class RequirementConverter implements CommandLine.ITypeConverter<Requirement> {
        public Requirement convert(String value) throws Exception {
            return RequirementUtils.load(value);
        }
    }

    @Option(names = "--debug", description = "enable debug logging")
    public void setDebug(boolean debug) {
        Configurator.setRootLevel(Level.DEBUG);
    }

    @Option(names = { "-v", "--verbose" }, description = "enable more verbose output")
    public void setVerbose(boolean verbose) {
        Configurator.setLevel("com.strider.datadefender", Level.INFO);
    }

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(String... args) throws Exception {
        CommandLine cmd = new CommandLine(new DataDefender());
        cmd.registerConverter(Requirement.class, new RequirementConverter());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
