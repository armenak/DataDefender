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