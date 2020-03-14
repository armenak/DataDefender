/*
 *
 * Copyright 2014, Armenak Grigoryan, Matthew Eaton, and individual contributors as indicated
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



package com.strider.datadefender.requirement.file;

import com.strider.datadefender.DataDefenderException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import static javax.xml.bind.JAXBContext.newInstance;

import com.strider.datadefender.database.DatabaseAnonymizerException;
import com.strider.datadefender.database.metadata.TableMetaData;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.Key;
import com.strider.datadefender.requirement.Key;
import com.strider.datadefender.requirement.Parameter;
import com.strider.datadefender.requirement.Parameter;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.Table;
import com.strider.datadefender.requirement.Table;

import lombok.extern.log4j.Log4j2;

/**
 * Utility class to help handling requirement objects
 * @author Matthew Eaton
 */
@Log4j2
public class Loader {

    /**
     * Load requirement XML file and return a Requirement object representing
     * it.
     *
     * @param requirementFile Requirement filename and path
     * @return Requirement object loaded based on file
     * @throws LoaderException
     */
    public static Requirement load(final String requirementFile) throws JAXBException, FileNotFoundException {

        Requirement requirement = null;
        log.info("Loading requirement file: {}", requirementFile);

        try {
            final JAXBContext  jc = newInstance(Requirement.class);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            requirement = (Requirement) unmarshaller.unmarshal(
                new FileInputStream(new File(requirementFile))
            );
        } catch (JAXBException je) {
            log.error("Unable to load XML from requirements file {}", je.getMessage());
            throw je;
        } catch (FileNotFoundException ex) {
            log.error("Requirement file not found, {}", ex.getMessage());
            throw ex;
        }

        return requirement;
    }
}
