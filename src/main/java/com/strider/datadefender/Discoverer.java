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
 *
 */



package com.strider.datadefender;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import static org.apache.log4j.Logger.getLogger;

import com.strider.datadefender.database.metadata.MatchMetaData;
import com.strider.datadefender.utils.RequirementUtils;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * Holds common logic for Discoverers.
 * @author Akira Matsuo
 */
public abstract class Discoverer {    // implements IDiscoverer {
    private static final Logger   log = getLogger(Discoverer.class);
    protected List<MatchMetaData> matches;

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

    /**
     * Creates model POJO based on OpenNLP model
     *
     * @param dataDiscoveryProperties
     * @param modelName
     * @return Model
     */
    public Model createModel(final Properties dataDiscoveryProperties, final String modelName) {
        InputStream          modelInToken = null;
        InputStream          modelIn      = null;
        TokenizerModel       modelToken;
        Tokenizer            tokenizer  = null;
        TokenNameFinderModel model      = null;
        NameFinderME         nameFinder = null;

        try {
            log.debug("Model name: " + modelName);
            modelInToken = new FileInputStream(dataDiscoveryProperties.getProperty("english_tokens"));
            log.debug(dataDiscoveryProperties.getProperty(modelName));
            modelIn    = new FileInputStream(dataDiscoveryProperties.getProperty(modelName));
            modelToken = new TokenizerModel(modelInToken);
            tokenizer  = new TokenizerME(modelToken);
            model      = new TokenNameFinderModel(modelIn);
            nameFinder = new NameFinderME(model);
            modelInToken.close();
            modelIn.close();
        } catch (FileNotFoundException ex) {
            log.error(ex.toString());

            try {
                if (modelInToken != null) {
                    modelInToken.close();
                }

                if (modelIn != null) {
                    modelIn.close();
                }
            } catch (IOException ioe) {
                log.error(ioe.toString());
            }
        } catch (IOException ex) {
            log.error(ex.toString());
        }

        return new Model(tokenizer, nameFinder, modelName);
    }

    public void createRequirement(final String fileName) throws DatabaseDiscoveryException {
        if ((matches == null) || matches.isEmpty()) {
            throw new DatabaseDiscoveryException("No matches to create requirement from!");
        }

        RequirementUtils.write(RequirementUtils.create(matches), fileName);
    }
}