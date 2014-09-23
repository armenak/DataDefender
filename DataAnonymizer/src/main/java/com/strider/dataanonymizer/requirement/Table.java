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
import static java.util.Collections.unmodifiableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * JAXB class that defines parameter table in Requirement.xml file
 * 
 * @author Armenak Grigoryan
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class Table {

    @XmlAttribute(name="Name")
    private String name;
    
    @XmlAttribute(name="PKey")
    private String pkey;    
    
    @XmlElementWrapper(name="Columns")
    @XmlElement(name="Column")
    private List<Column> columns;
    
    /**
     * Getter method for name attribute
     * @return String
     */
    public String getName()  {
        return this.name;
    }
    
    /**
     * Getter method for PKey attribute
     * @return String
     */
    public String getPKey()  {
        return this.pkey;
    }    
    
    /**
     * Getter method for columns attribute
     * @return 
     */
    public List<Column> getColumns() {
        if (this.columns != null) {
            return unmodifiableList(this.columns);
        }
        return null;
    }
}