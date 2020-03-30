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

import java.io.File;
import java.util.List;

import picocli.CommandLine.Option;

import lombok.Getter;

/**
 * Database configuration options for picocli.
 * 
 * @author Zaahid Bateson
 */
@Getter
public class ModelDiscoveryConfig {

    @Option(names = { "-l", "--limit" }, description = "Limit discovery to a set number of rows in a table", defaultValue = "1000")
    private Integer limit;

    @Option(names = { "-m", "--model" }, description = "Adds a built-in configured opennlp TokenizerME model for data discovery. "
                + "Available models are: ${AVAILABLE-MODELS}")
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
    private Boolean calculateScore = false;

    @Option(names = { "--threshold-count" }, description = "Reports if number of rows found are greater than the defined threshold", defaultValue = "6")
    private Integer thresholdCount;

    @Option(names = { "--threshold-high" }, description = "Reports if number of high risk columns found are greater than the defined threshold", defaultValue = "3")
    private Integer thresholdHighRisk;
}
