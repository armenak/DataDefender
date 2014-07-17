package com.strider.dataanonymizer.requirement;

import java.util.List;   
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Column {

    @XmlElement(name="Name")
    private String name;

    @XmlElement(name="Function")
    private String function;    
    
    @XmlElementWrapper(name="Parameters")
    @XmlElement(name="Parameter")
    private List<String> parameterList;
    
    public String getName() {
        return this.name;
    }
    
    public String getFunction() {
        return this.function;
    }    
    
    public List getParameterList() {
        return this.parameterList;
    }    

}