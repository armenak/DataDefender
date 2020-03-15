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

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * JAXB class that defines elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@XmlRootElement(name = "Requirement")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Requirement {
    @XmlElement(name = "Client")
    private String client;
    @XmlElement(name = "Version")
    private String version;
    @XmlElementWrapper(name = "Tables")
    @XmlElement(name = "Table")
    private List<Table> tables;

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
