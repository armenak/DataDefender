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



package com.strider.datadefender.requirement;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import static java.util.Collections.unmodifiableList;

/**
 * JAXB class that defines parameter table in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Table {
    @XmlAttribute(name = "Name")
    private String        name;
    @XmlAttribute(name = "PKey")
    private String        pkey;
    @XmlElementWrapper(name = "Columns")
    @XmlElement(name = "Column")
    private List<Column>  columns;
    @XmlElementWrapper(name = "PrimaryKey")
    @XmlElement(name = "Key")
    private List<Key>     primaryKeys;
    @XmlElementWrapper(name = "Exclusions")
    @XmlElement(name = "Exclude")
    private List<Exclude> exclusions;

    /**
     * Getter method for columns attribute
     * @return List<Column>
     */
    public List<Column> getColumns() {
        if (this.columns != null) {
            return unmodifiableList(this.columns);
        }

        return null;
    }

    public void setColumns(final List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Returns a list of exclusions
     *
     * @return List<Exclude>
     */
    public List<Exclude> getExclusions() {
        if (this.exclusions != null) {
            return unmodifiableList(this.exclusions);
        }

        return null;
    }

    public void setExclusions(final List<Exclude> exclusions) {
        this.exclusions = exclusions;
    }

    /**
     * Getter method for name attribute
     * @return String
     */
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Getter method for PKey attribute
     * @return String
     */
    public String getPKey() {
        return this.pkey;
    }

    // Setter methods
    public void setPkey(final String pkey) {
        this.pkey = pkey;
    }

    /**
     * Returns a List of keys defining the primary key.
     *
     * @return List<Key>
     */
    public List<Key> getPrimaryKeys() {
        if (this.primaryKeys != null) {
            return unmodifiableList(this.primaryKeys);
        }

        return null;
    }

    public void setPrimaryKeys(final List<Key> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
