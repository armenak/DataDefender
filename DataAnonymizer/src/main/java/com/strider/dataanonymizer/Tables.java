package com.strider.dataanonymizer;

import java.util.List;
import javax.xml.bind.annotation.*;

/**
 *
 * @author strider
 */
@XmlRootElement
public class Tables {
    private List<Table> items;

    @XmlElement(name="item")
    public List<Table> getItems() {
        return items;
    }

    public void setItems(List<Table> items) {
        this.items = items;
    }
    
}
