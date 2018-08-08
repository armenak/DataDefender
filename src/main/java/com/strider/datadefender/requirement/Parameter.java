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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

/**
 * JAXB class that defines parameter elements in Requirement.xml file
 * 
 * @author Armenak Grigoryan
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter {
    @XmlAttribute(name="Name")
    private String name;

    @XmlAttribute(name="Value")
    private String value;
    
    @XmlAttribute(name="Type")
    private String type;
    
    @XmlElement(name="Element")
    private List<ArrayElement> elements;
    
    /**
     * Getter method for name attribute
     * @return String
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Getter method for value attribute
     * @return String
     */
    public String getValue() {
        return this.value;
    }
    
    private Object getArrayForValues(final List<Object> list, final String type) throws ClassNotFoundException {
        String dataType = type;
        
        if ("String".equals(dataType)) {
            dataType = "java.lang.String";
        }
        final Class<?> c = ClassUtils.getClass(dataType);
        if (c.isPrimitive()) {
            final Class<?> w = ClassUtils.primitiveToWrapper(c);
            Object array = Array.newInstance(w, list.size());
            array = list.toArray((Object[]) array);
            if (c == boolean.class) {
                return ArrayUtils.toPrimitive((Boolean[]) array);
            } else if (c == byte.class) {
                return ArrayUtils.toPrimitive((Byte[]) array);
            } else if (c == short.class) {
                return ArrayUtils.toPrimitive((Short[]) array);
            } else if (c == char.class) {
                return ArrayUtils.toPrimitive((Character[]) array);
            } else if (c == int.class) {
                return ArrayUtils.toPrimitive((Integer[]) array);
            } else if (c == long.class) {
                return ArrayUtils.toPrimitive((Long[]) array);
            } else if (c == float.class) {
                return ArrayUtils.toPrimitive((Float[]) array);
            } else if (c == double.class) {
                return ArrayUtils.toPrimitive((Double[]) array);
            }
            throw new IllegalArgumentException("Unhandled primitive type: " + c.getName());
        }
        final Object array = Array.newInstance(c, list.size());
        return list.toArray((Object[]) array);
    }
    
    private Object getTypeValueOf(final String type, final String value)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {
        
        if (type.equals(boolean.class.getName())) {
            return Boolean.parseBoolean(value);
        } else if (type.equals(byte.class.getName())) {
            return Byte.parseByte(value);
        } else if (type.equals(short.class.getName())) {
            return Short.parseShort(value);
        } else if (type.equals(char.class.getName())) {
            return value.charAt(0);
        } else if (type.equals(int.class.getName())) {
            return Integer.parseInt(value);
        } else if (type.equals(long.class.getName())) {
            return Long.parseLong(value);
        } else if (type.equals(float.class.getName())) {
            return Float.parseFloat(value);
        } else if (type.equals(double.class.getName())) {
            return Double.parseDouble(value);
        } else if (type.equals(String.class.getName()) || "String".equals(type)) {
            return value;
        } else {
            final Constructor<?> constr = Class.forName(type).getConstructor(String.class);
            return constr.newInstance(value);
        }
    }
    
    public Object getTypeValue()
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {
        
        String typeName = type;
        if (typeName == null) {
            typeName = "String";
            if (elements != null && value == null) {
                typeName += "[]";
            }
        }
        
        if (typeName.endsWith("[]")) {
            if (elements == null) {
                return null;
            }
            final String arrayType = typeName.substring(0, typeName.length() - 2);
            final List<Object> arr = new ArrayList<>(elements.size());
            for (final ArrayElement el : elements) {
                arr.add(getTypeValueOf(arrayType, el.getValue()));
            }
            return getArrayForValues(arr, arrayType);
        }
        return getTypeValueOf(typeName, value);
    }
    
    /**
     * Getter method for type attribute
     * @return String
     */
    public String getType() {
        return this.type;
    }

    // Setter methods
    public void setElements(final List<ArrayElement> elements) {
        this.elements = elements;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setType(final String type) {
        this.type = type;
    }
}