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

package com.strider.datadefender.utils;

import static javax.xml.bind.JAXBContext.newInstance;
import static org.apache.log4j.Logger.getLogger;

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

import org.apache.log4j.Logger;

import com.strider.datadefender.database.DatabaseAnonymizerException;
import com.strider.datadefender.database.metadata.MatchMetaData;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.Key;
import com.strider.datadefender.requirement.Parameter;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.Table;

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
    
    /**
     * Write requirement to file.
     * @param requirement
     * @param fileName
     * @throws DatabaseAnonymizerException
     */
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
     * Create a requirement from sorted (by (schema.)table) List of matching columns.
     * @param matches
     * @return
     */
    public static Requirement create(List<MatchMetaData> matches) {
        Map<String, Table> tables = new HashMap<>();
        Map<String, List<Column>> columns = new HashMap<>();
        Column column;
        for (MatchMetaData match : matches) {
            String tableName = match.getTableName();
            if (match.getSchemaName() != null && !match.getSchemaName().equals("")) {
                tableName = match.getSchemaName() + "." + tableName;
            }
            Table table = tables.get(tableName);
            if (table == null) { // new table
                table = new Table();
                table.setName(tableName);
                List<String> pks = match.getPkeys();
                if (pks.size() == 1) { // only one pk
                    table.setPkey(pks.get(0));
                } else { // multiple key pk
                    List<Key> keys = pks.stream().map(pkName -> { 
                        Key key = new Key(); key.setName(pkName); return key; }).collect(Collectors.toList());
                    table.setPrimaryKeys(keys);
                } 
                // store table
                tables.put(tableName, table);
                columns.put(tableName, new ArrayList<>());
            } // deal with columns
            column = new Column();
            column.setName(match.getColumnName());
            column.setReturnType(match.getColumnType());
            addDefaultParam(column);
            columns.get(tableName).add(column); // add column
        } // add columns to tables
        for (Entry<String, List<Column>> entry: columns.entrySet()) {
            tables.get(entry.getKey()).setColumns(entry.getValue());
        }
        
        Requirement req = new Requirement();
        req.setClient("Autogenerated Template Client");
        req.setVersion("1.0"); // hopefully order of tables doesn't matter
        req.setTables(new ArrayList<>(tables.values()));
        return req;
    }
    
    // Hard-coded default params for now.
    private static void addDefaultParam(Column column) {
        column.setFunction("com.strider.dataanonymizer.functions.CoreFunctions.randomStringFromFile");
        List<Parameter> params = new ArrayList<>();
        Parameter param = new Parameter();
        param.setName("file");
        param.setValue("default-file.txt");
        param.setType("String");
        params.add(param);
        column.setParameters(params);
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
