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

import com.strider.datadefender.discoverer.Discoverer;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.file.Loader;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.TypeConversionException;
import picocli.CommandLine.UnmatchedArgumentException;

import lombok.extern.log4j.Log4j2;

/**
 * Entry point to Data Defender.
 *
 * This class will parse and analyze the parameters and execute appropriate
 * service.
 *
 * TODO(ZB): Look into setting up command mixins for debug/verbosity so they
 * don't have to appear before subcommands [https://picocli.info/#_mixins]
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "datadefender",
    mixinStandardHelpOptions = true,
    version = "2.0",
    description = "Data detection and anonymization tool",
    synopsisSubcommandLabel = "COMMAND",
    subcommands = {
        HelpCommand.class,
        Anonymize.class,
        Extract.class,
        Discover.class
    }
)
@Log4j2
public class DataDefender implements Callable<Integer> {

    // initializing system property used by ModelDiscoveryConfig option parameter help
    static {
        System.setProperty("AVAILABLE-MODELS", StringUtils.join(
            Discoverer.BUILT_IN_MODELS.keySet().stream().sorted().collect(Collectors.toList()),
            ", "
        ));
    }

    @Mixin
    private LogLevelConfig logLevels;

    /**
     * Copied from picocli documentation, presents a shorter "Usage" help when
     * there's an error in the options/arguments.
     *
     * https://picocli.info
     */
    public static class ShortErrorMessageHandler implements IParameterExceptionHandler {

        public int handleParseException(ParameterException ex, String[] args) {
            CommandLine cmd = ex.getCommandLine();
            PrintWriter writer = cmd.getErr();

            writer.println(ex.getMessage());
            UnmatchedArgumentException.printSuggestions(ex, writer);
            writer.print(cmd.getHelp().fullSynopsis()); // since 4.1

            CommandSpec spec = cmd.getCommandSpec();
            writer.printf("Try '%s --help' for more information.%n", spec.qualifiedName());

            return cmd.getExitCodeExceptionMapper() != null
                        ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                        : spec.exitCodeOnInvalidInput();
        }
    }

    public static class RequirementConverter implements CommandLine.ITypeConverter<Requirement> {
        public Requirement convert(String value) throws Exception {
            Loader loader = new Loader();
            try {
                return loader.load(value);
            } catch (FileNotFoundException e) {
                log.debug("Error loading requirements file", e);
                throw new TypeConversionException("Unable to load requirements file: " + e.getMessage());
            } catch (Exception e) {
                Throwable exc = e;
                if (StringUtils.isBlank(e.getMessage()) && e.getCause() != null) {
                    exc = e.getCause();
                }
                log.debug("Error loading requirements.", exc);
                throw new TypeConversionException("Unable to load requirements file: " + exc.getMessage());
            }
        }
    }

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static void main(String... args) throws Exception {
        CommandLine cmd = new CommandLine(new DataDefender())
            .registerConverter(Requirement.class, new RequirementConverter())
            .setParameterExceptionHandler(new ShortErrorMessageHandler());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
