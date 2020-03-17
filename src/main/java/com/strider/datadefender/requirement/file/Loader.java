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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import static javax.xml.bind.JAXBContext.newInstance;

import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.functions.RequirementFunctionClassRegistry;
import java.lang.reflect.InvocationTargetException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Utility class to help handling requirement objects
 * @author Matthew Eaton
 */
@Log4j2
@RequiredArgsConstructor
public class Loader {

    private final JAXBContext jaxbContext;
    private final Unmarshaller unmarshaller;

    public Loader() throws JAXBException {
        jaxbContext = newInstance(Requirement.class);
        unmarshaller = jaxbContext.createUnmarshaller();
    }

    /**
     * Load requirement XML file and return a Requirement object representing
     * it.
     *
     * @param requirementFile Requirement filename and path
     * @param version required version
     * @return Requirement object loaded based on file
     * @throws javax.xml.bind.JAXBException
     * @throws java.io.FileNotFoundException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public Requirement load(final String requirementFile, final String version) throws
        FileNotFoundException,
        JAXBException,
        NoSuchMethodException,
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {

        Requirement requirement = null;
        log.info("Loading requirement file: {}", requirementFile);

        requirement = (Requirement) unmarshaller.unmarshal(
            new FileInputStream(new File(requirementFile))
        );
        RequirementFunctionClassRegistry.singleton().register(requirement);
        if (!VersionUtil.isCompatible(version, requirement.getVersion())) {
            throw new IllegalArgumentException(String.format(
                "The Requirement XML file's version: %s, is incompatible with "
                + " the application's version: %s",
                requirement.getVersion(),
                version
            ));
        }
        return requirement;
    }
}
