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

import java.util.ArrayList;
import java.util.List;
import static java.util.Collections.unmodifiableList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB class that defines elements in Requirement.xml file
 * 
 * @author Armenak Grigoryan
 */

@XmlRootElement(name="Requirement")
@XmlAccessorType(XmlAccessType.FIELD)
public class Requirement {

    @XmlElement(name="Client")
    private String client;

    @XmlElement(name="Version")
    private String version;

    @XmlElementWrapper(name="Tables")
    @XmlElement(name="Table")
    private List<Table> tables;
    
    /**
     * Getter method for client attribute
     * @return String
     */
    public String getClient() 
    { 
        return this.client; 
    }

    /**
     * Getter method for version attribute
     * @return String
     */
    public String getVersion() {
        return this.version;
    }
    
    /**
     * Getter method for tables attribute
     * @return List
     */
    public List<Table> getTables() {
        List<Table> tableList = null;
        
        if (this.tables != null) {
            tableList = new ArrayList();
            tableList = unmodifiableList(this.tables);
        }
        return tableList;
    }
    
    // Setter methods
    public void setClient(final String client) {
        this.client = client;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setTables(final List<Table> tables) {
        this.tables = tables;
    }
}