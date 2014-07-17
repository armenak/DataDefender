package com.strider.dataanonymizer.requirement;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.*;

/**
 * JAXB class that defines elements of XML file
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
    
    public String getClient() 
    { 
        return this.client; 
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public List<Table> getTables() {
        return Collections.unmodifiableList(this.tables);
    }

}