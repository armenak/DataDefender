package com.strider.dataanonymizer.functions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author strider
 */
public class Functions {
    
    private static final List<String> firstNameList   = new ArrayList<>();
    private static final List<String> lastNameList   = new ArrayList<>();

    
    public static String randomFirstName(String fileName) throws IOException {
        
        if (firstNameList.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                for(String line; (line = br.readLine()) != null; ) {
                    firstNameList.add(line);
                }
            }            
        }
        
        int rand = nextIntInRange(0,firstNameList.size()-1);
        return firstNameList.get(rand);
    }
    
    public static String randomLastName(String fileName) throws IOException {
        
        if (lastNameList.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                for(String line; (line = br.readLine()) != null; ) {
                    lastNameList.add(line);
                }
            }            
        }
        
        int rand = nextIntInRange(0,lastNameList.size()-1);
        return lastNameList.get(rand);
    }    
    
    private static int nextIntInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
