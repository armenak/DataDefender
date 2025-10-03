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
package com.strider.datadefender.requirement;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * JAXB class that defines parameter table in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class Table {

    @XmlAttribute
    private String name;

    @XmlAttribute(name = "primary-key")
    private String primaryKey;

    @XmlElement
    private String where;

    @XmlElementWrapper(name = "primary-key")
    @XmlElement(name = "key")
    private List<String> primaryKeys;

    @XmlElementWrapper(name = "columns")
    @XmlElement(name = "column")
    private List<Column> columns;
    
    @XmlElementWrapper(name = "exclusions")
    @XmlElement(name = "exclude")
    private List<Exclude> exclusions;

    public Table() {
    }

    public Table(String name) {
        this.name = name;
    }

    public List<String> getPrimaryKeyColumnNames() {
        if (CollectionUtils.isNotEmpty(primaryKeys)) {
            return primaryKeys;
        }
        return List.of(primaryKey);
    }
}
