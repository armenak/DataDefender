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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * JAXB class that defines argument elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Log4j2
public class Argument {

    @XmlAttribute(name = "Name")
    private String name;

    @XmlAttribute(name = "Type")
    @XmlJavaTypeAdapter(ClassAdapter.class)
    private Class<?> type;

    @XmlAttribute(name = "Value")
    @Getter(AccessLevel.NONE)
    private String value;

    @XmlElement(name = "Element")
    private List<ArrayElement> elements;
    
    @Getter(AccessLevel.NONE)
    private Object objectValue;

    /**
     * If set to true, "Value" is null, and elements are empty, uses the passed
     * value -- either the running value in a function chain (the return value
     * from the last call) or the value of the column, or the ResultSet at the
     * current row if "Type" is java.sql.ResultSet.
     */
    @XmlAttribute(name = "IsDynamicValue")
    private boolean isDynamicValue = false;

    public Argument() {
    }

    public Argument(Class<?> type) {
        this.type = type;
    }

    /**
     * Sets up the value based on the type.
     *
     * @param unmarshaller
     * @param parent
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
        throws InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
        
        if (value != null) {
            log.debug("Converting value: {} to type: {}", value, type);
            objectValue = TypeConverter.convert(value, type);
        } else if (elements != null) {
            List a = new ArrayList(elements.size());
            for (ArrayElement el : elements) {
                a.add(TypeConverter.convert(el.getValue(), type));
            }
            objectValue = a;
        }
    }

    /**
     * Returns the value of the argument.
     *
     * The method is called with a dynamic value set from a previous invocation
     * in a chain of method calls, which is used if IsDynamicValue is set to
     * true.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public Object getValue(Object lastValue)
        throws InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
        
        if (isDynamicValue && value == null && elements == null) {
            log.debug("Using dynamic value for attribute");
            if (!type.isInstance(lastValue)) {
                log.debug("Converting dynamic attribute value: {} to type: {}", lastValue, type);
                return TypeConverter.convert(lastValue, type);
            }
            return lastValue;
        }
        return objectValue;
    }

    /**
     * Returns the String value of the attribute named "Value".
     */
    public String getValueAttribute() {
        return value;
    }
}
