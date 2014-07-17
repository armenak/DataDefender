package com.strider.dataanonymizer.requirement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class models Parameter element in Requirement XML file
 * @author Armenak Grigoryan
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter {
    @XmlAttribute(name="Name")
    private String name;

    @XmlAttribute(name="Value")
    private String value;    
    
    
    public String getName() {
        return this.name;
    }
    
    public String getValue() {
        return this.value;
    }        
}