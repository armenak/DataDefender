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
 *
 */
package com.strider.datadefender.requirement;

import com.strider.datadefender.requirement.plan.GlobalPlan;
import com.strider.datadefender.requirement.registry.ClassAndFunctionRegistry;

import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * JAXB class that defines elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@Data
@Log4j2
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "anonymizer")
public class Requirement {

    @XmlAccessorType(XmlAccessType.NONE)
    @Data
    public static class AutoresolvePackage {
        @XmlAttribute
        private String name;

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private ClassAndFunctionRegistry registry;

        public AutoresolvePackage() {
            this(ClassAndFunctionRegistry.singleton());
        }

        public AutoresolvePackage(ClassAndFunctionRegistry registry) {
            this.registry = registry;
        }

        public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
            log.debug("Found autoresolve package: {}", name);
            registry.registerAutoResolvePackage(name);
        }
    }

    @XmlElement(name = "anonymizer-version")
    private double anonymizerVersion = 2.0d;

    @XmlElement
    private String project;

    @XmlElement(name = "project-version")
    private String version;
    
    @XmlElementWrapper(name = "column-plans")
    @XmlElement(name = "plan")
    private List<GlobalPlan> plans;

    @XmlElementWrapper(name = "tables")
    @XmlElement(name = "table")
    private List<Table> tables;

    @XmlElementWrapper(name = "autoresolve-classes")
    @XmlElement(name = "package")
    private List<AutoresolvePackage> autoresolve;

    /**
     * Returns a list of Table elements that match entries in the passed filter.
     *
     * Attempts to filter out differences with schema, so 'schema.tablename'
     * matches 'tablename'.
     *
     * @param tables
     * @return
     */
    public List<Table> getFilteredTables(List<String> filter) {
        if (CollectionUtils.isEmpty(filter)) {
            return tables;
        }
        return tables.stream().filter((req) ->
            filter.stream().anyMatch((s) -> {
                String r = req.getName();
                if (s.equalsIgnoreCase(r)) {
                    return true;
                } else if (s.contains(".") && !r.contains(".")) {
                    return StringUtils.endsWithIgnoreCase(s, "." + r);
                } else if (r.contains(".") && !s.contains(".")) {
                    return StringUtils.endsWithIgnoreCase(r, "." + s);
                }
                return false;
            })
        ).collect(Collectors.toList());
    }
}
