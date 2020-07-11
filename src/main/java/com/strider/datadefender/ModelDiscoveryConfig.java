/*
 * Copyright 2014-2020, Armenak Grigoryan, and individual contributors as indicated
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
import java.io.File;
import java.util.List;
import java.util.Optional;

import picocli.CommandLine.Option;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Database configuration options for picocli.
 * 
 * @author Zaahid Bateson
 */
@Getter
@Log4j2
public class ModelDiscoveryConfig {

    @Option(names = { "-l", "--limit" }, description = "Limit discovery to a set number of rows in a table", defaultValue = "1000")
    private Integer limit;

    private List<String> models;

    @Option(names = { "-e", "--extension" }, description = "Adds a call to an extension method "
        + "(e.g. com.strider.datadefender.specialcase.SinDetector.detectSin)")
    private List<String> extensions;

    @Option(names = { "--model-file" }, description = "Adds a custom made opennlp TokenizerME file for data discovery.")
    private List<File> fileModels;

    @Option(names = { "--token-model" }, description = "Override the default built-in token model (English tokens, "
        + "en-token.bin) with a custom token file for use by opennlp's TokenizerModel")
    private File tokenModel;

    @Option(names = { "--probability-threshold" }, description = "Minimum NLP match score to return results for", defaultValue = "0.55")
    private Double probabilityThreshold;

    @Option(names = { "--no-score-calculation" }, description = "If set, includes a column score", negatable = true)
    private Boolean calculateScore = true;

    @Option(names = { "--threshold-count" }, description = "Reports if number of rows found are greater than the defined threshold", defaultValue = "6")
    private Integer thresholdCount;

    @Option(names = { "--threshold-high" }, description = "Reports if number of high risk columns found are greater than the defined threshold", defaultValue = "3")
    private Integer thresholdHighRisk;

    @Option(names = { "-m", "--model" }, description = "Adds a built-in configured opennlp TokenizerME model for data discovery. "
                + "Available models are: ${AVAILABLE-MODELS}")
    public void setModels(List<String> models) {
        Optional<String> unmatched = models.stream().filter((m) -> !Discoverer.BUILT_IN_MODELS.containsKey(m)).findFirst();
        if (unmatched.isPresent()) {
            log.error(
                "A built-in model with the name \"{}\" does not exist. Please specify one of: {}",
                unmatched.get(),
                System.getProperty("AVAILABLE-MODELS")
            );
            throw new IllegalArgumentException("Unmatched built-in model.");
        }
        this.models = models;
    }
}
