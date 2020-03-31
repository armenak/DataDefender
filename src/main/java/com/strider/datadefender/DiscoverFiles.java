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

import com.strider.datadefender.discoverer.FileDiscoverer;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;


/**
 * "discover" picocli subcommand, configures and executes the data discoverer.
 *
 * @author Zaahid Bateson
 */
@Command(
    name = "files",
    version = "1.0",
    mixinStandardHelpOptions = true,
    description = "Run file discovery utility"
)
@Log4j2
public class DiscoverFiles implements Callable<Integer> {

    @Mixin
    private LogLevelConfig logLevels;

    @ArgGroup(exclusive = false, multiplicity = "1", heading = "Model discovery settings%n")
    private ModelDiscoveryConfig modelDiscoveryConfig;

    @Option(names = { "-d", "--directory" }, description = "Adds a directory to list of directories to be scanned", required = true)
    private List<File> directories;

    @Option(names = { "-x", "--exclude-extension" }, description = "Adds an extension to exclude from data discovery", required = true)
    private List<String> excludeExtensions;

    @ParentCommand
    private Discover discover;

    @Override
    public Integer call() throws Exception {
        System.out.println("");
        System.out.println("Starting file discovery");
        log.warn("Discovery writes personal data to log files.");

        log.info("Probability threshold: {}", modelDiscoveryConfig.getProbabilityThreshold());
        log.info("Calculate score: {}", (modelDiscoveryConfig.getCalculateScore()) ? "yes" : "no");
        log.info("Threshold count: {}", modelDiscoveryConfig.getThresholdCount());
        log.info("Threshold high-risk count: {}", modelDiscoveryConfig.getThresholdHighRisk());
        log.info("Limit: {}", modelDiscoveryConfig.getLimit());
        log.info("Built-in models: {}", StringUtils.join(modelDiscoveryConfig.getModels(), ", "));
        log.info("Custom models: {}", StringUtils.join(modelDiscoveryConfig.getFileModels(), ", "));
        log.info("Custom token model: {}", modelDiscoveryConfig.getTokenModel());
        log.info("Extensions: {}", StringUtils.join(modelDiscoveryConfig.getExtensions(), ", "));
        log.info("Directories: {}", StringUtils.join(directories, ", "));
        log.info("File types not considered for analysis: {}", excludeExtensions);

        FileDiscoverer fd = new FileDiscoverer(modelDiscoveryConfig, directories, excludeExtensions);
        fd.discover();

        return 0;
    }
}
