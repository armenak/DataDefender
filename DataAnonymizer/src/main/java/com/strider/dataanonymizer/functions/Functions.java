package com.strider.dataanonymizer.functions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * @author strider
 */
public class Functions {
    
    static Logger log = Logger.getLogger(Functions.class);

    
    private static final List<String> firstNameList  = new ArrayList<>();
    private static final List<String> lastNameList   = new ArrayList<>();
    private static final List<String> words          = new ArrayList<String>();

    public static void init() {        
        try 
        {
            log.info("*** Adding list of words into array");
            addWordsIntoArray();
            log.info("*** Array is populated with words from dictionary");
        } 
        catch (Exception ode) 
        {
            log.error("Error occurred while reading dictionary.txt file.\n" + ode.toString());
        }
    }    
    
    
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
    
    public static String randomEmail(String domainName) 
    {
        StringBuilder email = new StringBuilder();
        email.append(generateRandomString(1,43).trim()).append("@").append(domainName);
        return email.toString();
    }    
    
    private static int nextIntInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
    
    public static String generateRandomString(int num, int length) 
    {
        List<Integer> randomNumbers = new ArrayList<>();
        Random random = new Random();

        for (int i=0;i<num;i++) {
            int rand = random.nextInt(479617);
            randomNumbers.add(rand);
        }

        StringBuilder randomString = new StringBuilder();
        for (Integer i : randomNumbers) {
            randomString.append(words.get(i));
            randomString.append(" ");
        }
        
        int stringLength = length;
        if (randomString.length() <= length)
        {
            stringLength = randomString.length();
        }
        
        return randomString.toString().substring(0, stringLength);
    }    
    
   private static void addWordsIntoArray() throws Exception
    {
        try (Scanner scanner = new Scanner(Functions.class.getClassLoader().getResourceAsStream("dictionary.txt"))) {
            while (scanner.hasNext())
            {
                words.add(scanner.next());
            }
        }
    }    
}
