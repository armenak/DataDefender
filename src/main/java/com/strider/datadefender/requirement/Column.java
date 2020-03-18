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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import static java.util.Collections.unmodifiableList;

/**
 * JAXB class that defines column elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@Log4j2
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Column {

    @XmlAttribute(name = "Name")
    private String name;

    @XmlAttribute(name = "IgnoreEmpty")
    private boolean ignoreEmpty;

    @XmlJavaTypeAdapter(ClassAdapter.class)
    @XmlAttribute(name = "Type")
    private Class<?> type;

    @XmlElementWrapper(name = "Functions", required = true)
    @XmlElement(name = "Function", required = true)
    private List<Function> functions;

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
        if (!CollectionUtils.isEmpty(exclusions)) {
            lst.addAll(exclusions);
        }
        if (ignoreEmpty) {
            lst.add(new Exclude(true));
        }
        return unmodifiableList(lst);
    }

    /**
     * Calls all functions defined under Functions in order.
     *
     * @param rs
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object invokeFunctionChain(ResultSet rs)
        throws SQLException,
        IllegalAccessException,
        InvocationTargetException,
        InstantiationException {
        
        Object returnedValue = null;
        Argument args = functions.get(0).getArguments().stream()
            .filter((a) -> a.isDynamicValue())
            .findFirst()
            .orElse(null);
        if (args != null) {
            if (ClassUtils.isAssignable(ResultSet.class, args.getType())) {
                returnedValue = rs;
            } else {
                returnedValue = rs.getObject(name, args.getType());
            }
        }
        for (Function fn : functions) {
            returnedValue = fn.invokeFunction(returnedValue);
        }
        return returnedValue;
    }

    /**
     * Uses functionName and parameters to find the method to associate with
     * 'Function'.
     *
     * @param unmarshaller
     * @param parent
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
        throws ClassNotFoundException {

        Class<?> chain = type;
        for (Function fn : functions) {
            Method m = fn.findFunction(chain);
            chain = m.getReturnType();
        }
    }
}
