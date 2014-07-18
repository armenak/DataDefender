package com.strider.dataanonymizer.requirement;

import java.util.Collections;
import java.util.List;   
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

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
    
    public String getName() {
        return this.name;
    }
    
    public String getFunction() {
        return this.function;
    }    
    
    public String getReturnType() {
        return this.returnType;
    }    
    
    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(this.paramters);
    }    

}