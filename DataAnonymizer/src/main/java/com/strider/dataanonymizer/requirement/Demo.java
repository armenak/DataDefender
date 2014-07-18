package com.strider.dataanonymizer.requirement;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class Demo {

    public static void main(String[] args) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(Requirement.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Requirement requirement = (Requirement) unmarshaller.unmarshal(new File("src/main/resources/Requirement.xml"));
        
        System.out.println(requirement.getClient());
        System.out.println(requirement.getVersion());
        
        for(Table table : requirement.getTables()) {
            System.out.println(table.getName());
            for(Column column : table.getColumns()) {
                System.out.println("    " + column.getFunction());
                System.out.println("    " + column.getName());
                for(Parameter parameter : column.getParameters()) {
                    System.out.println("    " + parameter.getName());
                    System.out.println("    " + parameter.getValue());
                    
                    
                }                
            }
        }        
    }
}