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

import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * JAXB class that defines column elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Column {
    @XmlAttribute(name = "Name")
    private String name;
    @XmlAttribute(name = "IgnoreEmpty")
    private boolean ignoreEmpty;
    @XmlElement(name = "Function")
    private String function;
    @XmlElement(name = "ReturnType")
    private String returnType;
    @XmlElementWrapper(name = "Parameters")
    @XmlElement(name = "Parameter")
    private List<Parameter> parameters;
    @XmlElementWrapper(name = "Exclusions")
    @XmlElement(name = "Exclude")
    private List<Exclude> exclusions;

    /**
     * Returns a list of exclusions
     *
     * @return List<Exclude>
     */
    public List<Exclude> getExclusions() {
        final List<Exclude> lst = new ArrayList<>();
        CollectionUtils.addAll(lst, exclusions);
        if (ignoreEmpty) {
            lst.add(new Exclude(true));
        }
        return unmodifiableList(lst);
    }
}
