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

package com.strider.dataanonymizer.requirement;

import java.util.List;
import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * JAXB class that defines column elements in Requirement.xml file
 * 
 * @author Armenak Grigoryan
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Column {

    @XmlAttribute(name="Name")
    private String name;

    @XmlAttribute(name="IgnoreEmpty")
    private String ignoreEmpty;
    
    @XmlElement(name="Function")
    private String function;    
    
    @XmlElement(name="ReturnType")
    private String returnType;
    
    @XmlElementWrapper(name="Parameters")
    @XmlElement(name="Parameter")
    private List<Parameter> paramters;
    
    @XmlElementWrapper(name="Exclusions")
    @XmlElement(name="Exclude")
    private List<Exclude> exclusions;
    
    /**
     * Getter method for name attribute
     * @return String
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Getter method for function attribute
     * @return String
     */
    public String getFunction() {
        return this.function;
    }    
    
    /**
     * Getter method for returnType attribute
     * @return String
     */
    public String getReturnType() {
        return this.returnType;
    }
    
    /**
     * Getter method for inoreEmpty attribute
     * @return boolean
     */
    public boolean isIgnoreEmpty() {
        return (this.ignoreEmpty != null && this.ignoreEmpty.equals("true"));
    }
    
    /**
     * Returns a list of exclusions
     * 
     * @return List<Exclude>
     */
    public List<Exclude> getExclusions() {
        if (exclusions == null && ignoreEmpty != null && ignoreEmpty.equals("true")) {
            Exclude exc = new Exclude();
            exc.setIgnoreEmpty();
            exclusions = new ArrayList<>();
            exclusions.add(exc);
        }
        if (this.exclusions != null) {
            return unmodifiableList(this.exclusions);
        }
        return null;
    }
    
    /**
     * Getter method for Parameters attribute
     * @return List
     */
    public List<Parameter> getParameters() {
        if (this.paramters != null) {
            return unmodifiableList(this.paramters);
        }
        return null;
    }
}
