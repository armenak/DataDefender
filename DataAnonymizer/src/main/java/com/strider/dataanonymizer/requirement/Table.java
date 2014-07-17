package com.strider.dataanonymizer.requirement;

import java.util.Collections;
import java.util.List;   
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class Table {

    @XmlAttribute(name="name")
    private String name;
    
    @XmlElementWrapper(name="Columns")
    @XmlElement(name="Column")
    private List<Column> columns;
    
    public String getName()  {
        return this.name;
    }
    
    public List<Column> getColumns() {
        return Collections.unmodifiableList(this.columns);
    }

}
