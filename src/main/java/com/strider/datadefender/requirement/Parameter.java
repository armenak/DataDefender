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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * JAXB class that defines parameter elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Log4j2
public class Parameter {

    @XmlAttribute(name = "Name")
    private String name;

    @XmlAttribute(name = "Type")
    @XmlJavaTypeAdapter(ClassAdapter.class)
    private Class<?> type;

    @XmlAttribute(name = "Value")
    @Getter(AccessLevel.NONE)
    private String value;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Object objectValue;

    @XmlAttribute(name = "UseRSColumnValue")
    private boolean useRsColumnValue = false;

    @XmlAttribute(name = "UseRSRow")
    private boolean useRsRow = false;

    @XmlElement(name = "Element")
    private List<ArrayElement> elements;

    /**
     * Sets up the value based on the type.
     *
     * @param unmarshaller
     * @param parent
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        Column c = (Column) parent;
        if (useRsRow || StringUtils.equals("@@row@@", value)) {
            objectValue = new ResultSetValue(ResultSet.class, c.getName());
        } else if (useRsColumnValue || StringUtils.equals("@@column@@", value)) {
            objectValue = new ResultSetValue(type, c.getName());
        } else if (elements != null) {
            List a = new ArrayList(elements.size());
            for (ArrayElement el : elements) {
                a.add(ConvertUtils.convert(el.getValue(), type));
            }
            objectValue = a;
        } else {
            objectValue = ConvertUtils.convert(value, type);
        }
    }

    /**
     * Returns the value of the parameter.  Requires a ResultSet for dynamic
     * parameter values set with UseRSColumnValue or UseRSRow.
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public Object getValue(ResultSet rs) throws SQLException {
        if (objectValue instanceof ResultSetValue) {
            ResultSetValue rsValue = (ResultSetValue) objectValue;
            return rsValue.getValue(rs);
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
