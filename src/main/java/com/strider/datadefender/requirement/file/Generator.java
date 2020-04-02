/*
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
 */
package com.strider.datadefender.requirement.file;

import com.strider.datadefender.database.DatabaseException;
import com.strider.datadefender.database.metadata.TableMetaData;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;
import com.strider.datadefender.requirement.Column;
import com.strider.datadefender.requirement.plan.Argument;
import com.strider.datadefender.requirement.plan.Function;
import com.strider.datadefender.requirement.Requirement;
import com.strider.datadefender.requirement.Requirement.AutoresolvePackage;
import com.strider.datadefender.requirement.Table;
import com.strider.datadefender.requirement.plan.GlobalPlan;
import com.strider.datadefender.requirement.plan.PlanRef;
import com.strider.datadefender.requirement.registry.ClassAndFunctionRegistry;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import lombok.extern.log4j.Log4j2;

/**
 * Utility class to help handling requirement objects
 * @author Matthew Eaton
 */
@Log4j2
public class Generator {

    private static Map<String, GlobalPlan> plans = Map.of(
        "lipsum-similar", createGlobalPlan(
            "lipsum-similar",
            new Function("Lipsum#similar", false),
            List.of(new Argument("text", String.class, true))
        ),
        "core-date", createGlobalPlan(
            "core-date",
            new Function("Core#randomDate", false),
            List.of(
                new Argument("start", String.class, "1970-01-01"),
                new Argument("end", String.class, "2005-01-01"),
                new Argument("format", String.class, "yyyy-MM-dd")
            )
        ),
        "core-date-time", createGlobalPlan(
            "core-date-time",
            new Function("Core#randomDateTime", false),
            List.of(
                new Argument("start", String.class, "2010-01-01 00:00:00"),
                new Argument("end", String.class, "2020-04-01 00:00:00"),
                new Argument("format", String.class, "yyyy-MM-dd HH:mm:ss")
            )
        )
    );

    private static GlobalPlan createGlobalPlan(String name, Function fn, List<Argument> arguments) {
        GlobalPlan plan = new GlobalPlan(name);
        fn.setArguments(arguments);
        plan.setFunctions(List.of(fn));
        return plan;
    }

    private static void setDefaultFunction(final String table, final Column column) {
        if (Objects.equals(column.getType(), Date.class)) {
            column.setPlanRef(new PlanRef(plans.get("core-date")));
        } else if (Objects.equals(column.getType(), Timestamp.class)) {
            column.setPlanRef(new PlanRef(plans.get("core-date-time")));
        } else {
            column.setPlanRef(new PlanRef(plans.get("lipsum-similar")));
        }
    }

    /**
     * Create a requirement from sorted (by (schema.)table) List of matching columns.
     * @param matches
     * @return
     */
    public static Requirement create(final Collection<ColumnMetaData> matches) {

        List<Table> tables = new ArrayList<>();
        List<TableMetaData> tableMetas = matches.stream()
            .map((c) -> c.getTable()).collect(Collectors.toSet())
            .stream().sorted().collect(Collectors.toList());
        for (final TableMetaData tMeta : tableMetas) {
            Table table = new Table(tMeta.getCanonicalTableName());
            final List<ColumnMetaData> pks = tMeta.getPrimaryKeys();
            if (pks.size() == 0) {
                table.setPrimaryKey("NONE-SET");
            } else if (pks.size() == 1) {    // only one pk
                table.setPrimaryKey(pks.get(0).getColumnName());
            } else {                  // multiple key pk
                final List<String> keys = pks.stream().map((col) -> col.getColumnName()).collect(Collectors.toList());
                table.setPrimaryKeys(keys);
            }
            table.setColumns(
                matches.stream()
                .filter((c) -> c.getTable() == tMeta)
                .sorted()
                .map((c) -> {
                    Column column = new Column(c.getColumnName());
                    column.setType(c.getColumnType());
                    setDefaultFunction(table.getName(), column);
                    return column;
                })
                .collect(Collectors.toList())
            );
            tables.add(table);
        }

        final Requirement req = new Requirement();
        req.setPlans(List.copyOf(plans.values()));
        ClassAndFunctionRegistry.singleton().clearAutoResolvePackages();
        req.setAutoresolve(List.of(
            new AutoresolvePackage("java.lang", true),
            new AutoresolvePackage("org.apache.commons.lang3", true),
            new AutoresolvePackage("com.strider.datadefender.anonymizer.functions", true)
        ));

        req.setProject("Autogenerated Template Project");
        req.setVersion("1.0");
        req.setTables(tables);

        return req;
    }

    /**
     * Write requirement to file.
     * @param requirement
     * @param outFile
     * @throws DatabaseException
     */
    public static void write(final Requirement requirement, final File outFile) throws DatabaseException, JAXBException, SAXException {
        log.info("Requirement.write() to file: " + outFile.getName());

        final JAXBContext jc = JAXBContext.newInstance(Requirement.class);
        final Marshaller  marshaller = jc.createMarshaller();
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(Generator.class.getResource("requirement.xsd"));

        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(requirement, outFile);
    }

    /**
     * Returns argument of name "file" if exists else returns null
     * @param arguments List of function arguments
     * @return File parameter object
     */
    public static Argument getFileParameter(final List<Argument> arguments) {
        return CollectionUtils
            .emptyIfNull(arguments)
            .stream()
            .filter((a) -> StringUtils.equals(a.getName(), "file"))
            .findAny()
            .orElse(null);
    }
}
