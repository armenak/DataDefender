/*
 *
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
package com.strider.datadefender.discoverer;

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.ModelDiscoveryConfig;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.requirement.file.Generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * Holds common logic for Discoverers.
 * @author Akira Matsuo
 */
@Log4j2
public abstract class Discoverer {

    public static final String DEFAULT_TOKEN_MODEL = "en-token.bin";
    public static final Map<String, String> BUILT_IN_MODELS = Map.of(
        "date", "en-ner-date.bin",
        "location", "en-ner-location.bin",
        "money", "en-ner-money.bin",
        "organization", "en-ner-organization.bin",
        "person", "en-ner-person.bin",
        "time", "en-ner-time.bin"
    );
    
    @Data
    public static class ColumnMatch {
        final private ColumnMetaData column;
        final private double averageProbability;
        final private String model;
        final private List<Probability> probabilityList;
    }

    protected List<ColumnMatch> matches;

    protected final ModelDiscoveryConfig config;
    protected final TokenizerME tokenizer;

    public Discoverer(ModelDiscoveryConfig config) throws IOException {
        this.config = config;
        if (config.getTokenModel() != null) {
            tokenizer = new TokenizerME(new TokenizerModel(config.getTokenModel()));
        } else {
            try (InputStream stream = Discoverer.class.getResourceAsStream(DEFAULT_TOKEN_MODEL)) {
                tokenizer = new TokenizerME(new TokenizerModel(stream));
            }
        }
    }

    public double calculateAverage(final List<Probability> values) {
        Double sum = 0.0;

        if (!values.isEmpty()) {
            for (final Probability value : values) {
                sum += value.getProbabilityValue();
            }

            return sum / values.size();
        }

        return sum;
    }

    private Model createModelFrom(TokenNameFinderModel tnf, String modelName) {
        NameFinderME nameFinder = new NameFinderME(tnf);
        return new Model(tokenizer, nameFinder, modelName);
    }

    /**
     * Creates model POJO based on OpenNLP model file
     *
     * @param modelName
     * @return Model
     */
    public Model createModel(final File modelFile) throws IOException {
        return createModelFrom(new TokenNameFinderModel(modelFile), modelFile.getName());
    }

    /**
     * Creates model POJO based on a built-in OpenNLP model
     *
     * @param modelName
     * @return Model
     */
    public Model createModel(final String modelName) throws IOException {
        try (InputStream stream = Discoverer.class.getResourceAsStream(BUILT_IN_MODELS.get(modelName))) {
            return createModelFrom(new TokenNameFinderModel(stream), modelName);
        }
    }
}