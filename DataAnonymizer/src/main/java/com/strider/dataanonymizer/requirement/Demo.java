package com.strider.dataanonymizer.requirement;

import java.io.File;
import java.util.List;
import javax.xml.bind.*;

public class Demo {

    public static void main(String[] args) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Requirement.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Requirement requirement = (Requirement) unmarshaller.unmarshal(new File("src/main/resources/Requirement.xml"));
        
        System.out.println(requirement.getClient());
        System.out.println(requirement.getVersion());
        
        for(Table table : requirement.getTables()) {
            System.out.println(table.getParentId());
            System.out.println(table.getName());
            for(String column : table.getColumns()) {
                System.out.println("    " + column);
            }
        }        
        
        //Table table = (Table)requirement.getTables().get(0);
        //System.out.println(table.getTableName());  
        
        
        //System.out.println(table.getColumnList().get(0));

//        Marshaller marshaller = jc.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        marshaller.marshal(fosterHome, System.out);
    }

}