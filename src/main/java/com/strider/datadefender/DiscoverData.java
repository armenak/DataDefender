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
import com.strider.datadefender.discoverer.DatabaseDiscoverer;
import com.strider.datadefender.discoverer.Discoverer;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.Getter;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import lombok.extern.log4j.Log4j2;

/**
 * "discover" picocli subcommand, configures and executes the data discoverer.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "data",
    version = "2.0",
    mixinStandardHelpOptions = true,
    description = "Run data discovery utility"
)
@Log4j2
public class DiscoverData implements Callable<Integer>, IRequirementCommand {

    static {
        System.setProperty("AVAILABLE-MODELS", StringUtils.join(
            Discoverer.BUILT_IN_MODELS.keySet().stream().sorted().collect(Collectors.toList()),
            ", "
        ));
    }

    @ParentCommand
    private Discover discover;

    @Spec
    private CommandSpec spec;

    @ArgGroup(exclusive = false, multiplicity = "1")
    private ModelDiscoveryConfig modelDiscoveryConfig;

    private List<ColumnMetaData> results;

    @Override
    public Integer call() throws Exception {

        DbConfig dbConfig = discover.getDbConfig();

        System.out.println("");
        System.out.println("Starting data discovery");
        log.info("Datasource URL: {}, vendor: {}, schema: {}", dbConfig.getUrl(), dbConfig.getVendor(), dbConfig.getSchema());
        log.info("Username: {}, Password provided: {}", dbConfig.getUsername(), (StringUtils.isNotBlank(dbConfig.getPassword()) ? "yes" : "no"));
        log.info("Probability threshold: {}", modelDiscoveryConfig.getProbabilityThreshold());
        log.info("Calculate score: {}", (modelDiscoveryConfig.getCalculateScore()) ? "yes" : "no");
        log.info("Threshold count: {}", modelDiscoveryConfig.getThresholdCount());
        log.info("Threshold high-risk count: {}", modelDiscoveryConfig.getThresholdHighRisk());
        log.info("Limit: {}", modelDiscoveryConfig.getLimit());
        log.info("Built-in models: {}", StringUtils.join(modelDiscoveryConfig.getModels(), ", "));
        log.info("Custom models: {}", StringUtils.join(modelDiscoveryConfig.getFileModels(), ", "));
        log.info("Custom token model: {}", modelDiscoveryConfig.getTokenModel());
        log.info("Extensions: {}", StringUtils.join(modelDiscoveryConfig.getExtensions(), ", "));

        IDbFactory factory = IDbFactory.get(dbConfig);
        DatabaseDiscoverer dd = new DatabaseDiscoverer(modelDiscoveryConfig, factory);
        results = dd.discover();

        discover.afterSubcommand();
        return 0;
    }

    @Override
    public List<ColumnMetaData> getColumnMetaData() {
        return results;
    }
}
