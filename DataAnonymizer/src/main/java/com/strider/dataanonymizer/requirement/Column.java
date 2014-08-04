package com.strider.dataanonymizer.requirement;

import java.util.List;   
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

    @XmlElement(name="Function")
    private String function;    
    
    @XmlElement(name="ReturnType")
    private String returnType;        
    
    @XmlElementWrapper(name="Parameters")
    @XmlElement(name="Parameter")
    private List<Parameter> paramters;
    
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