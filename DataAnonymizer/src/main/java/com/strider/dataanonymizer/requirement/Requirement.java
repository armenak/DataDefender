package com.strider.dataanonymizer.requirement;

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
        if (this.tables != null) {
            return unmodifiableList(this.tables);
        }
        return null;
    }
}