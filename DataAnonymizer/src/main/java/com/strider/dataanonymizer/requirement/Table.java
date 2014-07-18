package com.strider.dataanonymizer.requirement;

import java.util.Collections;
import java.util.List;   
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
            return Collections.unmodifiableList(this.columns);
        }
        return null;
    }
}