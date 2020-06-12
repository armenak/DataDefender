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
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.discoverer.ColumnDiscoverer;
import java.io.File;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;


/**
 * "discover" picocli subcommand, configures and executes the data discoverer.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "columns",
    version = "1.0",
    mixinStandardHelpOptions = true,
    description = "Run column discovery utility"
)
@Log4j2
public class DiscoverColumns implements Callable<Integer>, IRequirementCommand {

    private File outputFile;

    @Option(names = { "--column-pattern" }, description = "Regex pattern(s) to match column names", required = true)
    private List<Pattern> patterns;

    @ArgGroup(exclusive = false, multiplicity = "0..1", heading = "Database connection settings%n")
    private DbConfig dbConfig;

    @ParentCommand
    private Discover discover;

    @Spec
    private CommandSpec spec;

    private List<ColumnMetaData> results;

    @Option(names = { "-o", "--output" }, description = "Generate a requirements xml file and write it out to the specified file")
    public void setOutputFile(File f) {
        discover.setOutputFile(f);
    }

    @Override
    public Integer call() throws Exception {

        if (dbConfig == null) {
            dbConfig = discover.getDbConfig();
        } else {
            discover.setDbConfig(dbConfig);
        }

        System.out.println("Starting column discovery");
        log.warn("Discovery writes personal data to log files.");
        log.info("Datasource URL: {}, vendor: {}, schema: {}", dbConfig.getUrl(), dbConfig.getVendor(), dbConfig.getSchema());
        log.info("Username: {}, Password provided: {}", dbConfig.getUsername(), (StringUtils.isNotBlank(dbConfig.getPassword()) ? "yes" : "no"));
        
        IDbFactory factory = IDbFactory.get(dbConfig);
        ColumnDiscoverer discoverer = new ColumnDiscoverer(factory, patterns);

        try {
            results = discoverer.discover();
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug("Exception occurred during anonymization", e);
            return 1;
        }

        discover.afterSubcommand();

        return 0;
    }

    @Override
    public List<ColumnMetaData> getColumnMetaData() {
        return results;
    }
}
