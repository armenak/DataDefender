package com.strider.dataanonymizer.requirement;

import java.util.Collections;
import java.util.List;   
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Table {

    @XmlElement(name="ParentID")
    private String parentID;

    @XmlAttribute(name="name")
    private String name;
    
    @XmlElementWrapper(name="Columns")
    @XmlElement(name="Column")
    private List<String> columns;
    
    public String getParentId() {
        return this.parentID;
    }
    
    public String getName()  {
        return this.name;
    }
    
    public List<String> getColumns() {
        return Collections.unmodifiableList(this.columns);
    }

}
