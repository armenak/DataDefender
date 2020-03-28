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

import java.util.concurrent.Callable;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.RunAll;
import picocli.CommandLine.Spec;

import lombok.extern.log4j.Log4j2;

/**
 * "discover" picocli subcommand, configures and executes the data discoverer.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "files",
    version = "2.0",
    description = "Run file discovery utility"
)
@Log4j2
public class DiscoverFiles implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("Starting data discovery");
        return 0;
    }
}
