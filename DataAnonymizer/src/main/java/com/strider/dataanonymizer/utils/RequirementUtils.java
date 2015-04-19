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

package com.strider.dataanonymizer.utils;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.requirement.Parameter;
import com.strider.dataanonymizer.requirement.Requirement;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static javax.xml.bind.JAXBContext.newInstance;
import static org.apache.log4j.Logger.getLogger;

/**
 * Utility class to help handling requirement objects
 * @author Matthew Eaton
 */
public class RequirementUtils {

    private static Logger log = getLogger(RequirementUtils.class);

    /**
     * Requirement file parameter name
     */
    public static final String  PARAM_NAME_FILE = "file";

    /**
     * Load requirement file into java objects
     * @param requirementFile Requirement filename and path
     * @return Requirement object loaded based on file
     * @throws DatabaseAnonymizerException
     */
    public static Requirement load(String requirementFile) throws DatabaseAnonymizerException {
        // Now we collect data from the requirement
        Requirement requirement = null;
        log.info("Requirement.load() file: " + requirementFile);

        try {
            JAXBContext jc = newInstance(Requirement.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            requirement = (Requirement) unmarshaller.unmarshal(new FileInputStream(new File(requirementFile)));
        } catch (JAXBException je) {
            log.error(je.toString());
            throw new DatabaseAnonymizerException(je.toString(), je);
        } catch (FileNotFoundException ex) {
            log.error("Requirement file not found", ex);
        }

        return requirement;
    }
    
    public static void write(Requirement requirement, String fileName) throws DatabaseAnonymizerException {
        log.info("Requirement.write() to file: " + fileName);
        File outFile = new File(fileName);

        try {
            JAXBContext jc = newInstance(Requirement.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(requirement, outFile);
        } catch (JAXBException je) {
            log.error(je.toString());
            throw new DatabaseAnonymizerException(je.toString(), je);
        }
    }

    /**
     * Returns Parameter of name "file" if exists else returns null
     * @param parameters List of column parameters
     * @return File parameter object
     */
    public static Parameter getFileParameter(List<Parameter> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            for (Parameter parameter : parameters) {
                if (PARAM_NAME_FILE.equalsIgnoreCase(parameter.getName())) {
                    return parameter;
                }
            }
        }

        return null;
    }
}
