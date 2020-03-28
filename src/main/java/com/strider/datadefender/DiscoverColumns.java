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

import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.discoverer.ColumnDiscoverer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import lombok.extern.log4j.Log4j2;


/**
 * "discover" picocli subcommand, configures and executes the data discoverer.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "columns",
    version = "2.0",
    description = "Run column discovery utility"
)
@Log4j2
public class DiscoverColumns implements Callable<Integer> {

    @ParentCommand
    private Discover discover;

    @Option(names = { "--column-pattern" }, description = "Regex pattern(s) to match column names", required = true)
    private List<Pattern> patterns;

    @Override
    public Integer call() throws Exception {
        System.out.println("Starting column discovery");
        
        IDbFactory factory = IDbFactory.get(discover.getDbConfig());
        ColumnDiscoverer discoverer = new ColumnDiscoverer(factory, patterns);

        try {
            discoverer.discover();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug("Exception occurred during anonymization", e);
            return 1;
        }

        return 0;
    }
}
