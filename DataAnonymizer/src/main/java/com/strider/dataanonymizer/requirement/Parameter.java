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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

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
    
    /**
     * Getter method for type attribute
     * @return String
     */
    public String getType() {
        return this.type;
    }    
}