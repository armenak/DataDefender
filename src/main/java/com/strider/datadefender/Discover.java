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

import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

import lombok.extern.log4j.Log4j2;
import lombok.Getter;

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
    version = "2.0",
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

    @ArgGroup(exclusive = false, multiplicity = "0..1")
    @Getter
    private DbConfig dbConfig;

    @Override
    public Integer call() throws Exception {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
