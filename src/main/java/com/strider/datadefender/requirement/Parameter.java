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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ClassUtils;

import lombok.Data;

/**
 * JAXB class that defines parameter elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Parameter {
    @XmlAttribute(name = "Name")
    private String name;
    @XmlAttribute(name = "Value")
    private String value;
    @XmlAttribute(name = "Type")
    private String type;
    @XmlElement(name = "Element")
    private List<ArrayElement> elements;

    /**
     * Converts a Parameter element to an Object based on Type and Value, or
     * Type and Elements for an array.
     * 
     * @return
     * @throws ClassNotFoundException
     */
    public Object getTypeValue() throws ClassNotFoundException {
        
        String t = StringUtils.trimToEmpty(type);
        if (StringUtils.isBlank(t)) {
            t = "java.lang.String";
        } else if (!t.contains(".") && Character.isUpperCase(t.charAt(0))) {
            t = "java.lang." + t;
        }
        if (elements != null && value == null) {
            if (t.endsWith("[]")) {
                t = t.substring(0, t.length() - 2);
            }
            List a = new ArrayList(elements.size());
            for (ArrayElement el : elements) {
                a.add(ConvertUtils.convert(el.getValue(), ClassUtils.getClass(t)));
            }
        }
        return ConvertUtils.convert(value, ClassUtils.getClass(t));
    }
}
