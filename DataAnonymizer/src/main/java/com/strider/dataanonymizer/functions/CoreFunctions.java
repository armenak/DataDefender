/*
 * 
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package com.strider.dataanonymizer.functions;

import com.strider.dataanonymizer.utils.Xeger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Armenak Grigoryan
 */
public class CoreFunctions {
    
    private static Logger log = getLogger(CoreFunctions.class);

    private static Map<String, List<String>> fileList = new HashMap<String, List<String>>();
    private static List<String> words          = new ArrayList<>();

    static {        
        try  {
            log.info("*** Adding list of words into array");
            addWordsIntoArray();
            log.info("*** Array is populated with words from dictionary");
        } catch (Exception ode) {
            log.error("Error occurred while reading dictionary.txt file.\n" + ode.toString());
        }
    }    
    
    public CoreFunctions() {
    }
    
    public String generateStringFromPattern(String ... params) {
        String regex = params[0];
        Xeger instance = new Xeger(regex);
        return instance.generate();
    }
    
    /**
     * Generates a list of random strings from a list of strings (new-
     * line separated) in a file.
     * 
     * @param params The first parameter is the filename
     * @return A random string from the file
     */
    public String randomStringFromFile(String ... params) throws IOException {
		String fileName = params[0];
		log.info("*** random string from " + fileName);
		if (!fileList.containsKey(fileName)) {
			log.info("*** reading from " + fileName);
			List<String> values = new ArrayList<String>();
			try (BufferedReader br = new BufferedReader(new FileReader(params[0]))) {
                for (String line; (line = br.readLine()) != null; ) {
                    values.add(line);
                }
                
            }
            fileList.put(fileName, values);
		}
		log.info("*** selecting random string from " + fileName);
		List<String> values = fileList.get(fileName);
		int rand = nextIntInRange(0, values.size() - 1);
		return values.get(rand);
	}
    
    public String randomFirstName(String ... params) throws IOException {
		return randomStringFromFile(params);
    }
    
    public String randomLastName(String ... params) throws IOException {        
        return randomStringFromFile(params);
    }    
    
    public String randomMiddleName(String ... params) throws IOException {        
        return randomStringFromFile(params);
    }        
    
    public String randomEmail(String ... params) {
        StringBuilder email = new StringBuilder();
        email.append(generateRandomString(1,43).trim()).append("@").append(params[0]);
        return email.toString();
    }    
    
    /**
     * Returns email address sent as a parameter 
     * @param params
     * @return email address String 
     */
    public String staticEmail(String ... params) {
        return params[0];
    }        

    /**
     * Generates random postal code
     * @param params
     * @return String Random postal code
     * @throws IOException 
     */
    public String randomPostalCode(String ... params) throws IOException {
        return randomStringFromFile(params);
    }

    public String randomCity(String ... params) throws IOException {
        return randomStringFromFile(params);
    }
    
    public String randomStreet(String ... params) throws IOException {
        return randomStringFromFile(params);
    }    
    
    public String randomState(String ... params)  throws IOException {
        return randomStringFromFile(params);
    }        
    
    private int nextIntInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
    
    public String generateRandomString(int num, int length) {
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
        if (randomString.length() <= length) {
            stringLength = randomString.length();
        }
        
        return randomString.toString().substring(0, stringLength);
    }    
    
    public String randomDescription(String ... params) {
                
        if (!params[0].isEmpty() && !params[1].isEmpty() ) {
            if (Utils.isInteger(params[0]) && Utils.isInteger(params[1])) {
                StringBuilder desc = new StringBuilder();
                desc.append(generateRandomString(parseInt(params[0]), parseInt(params[1])).trim());
                return desc.toString();
            }
        }
        return "";
    }    
 
    public String randomPhoneNumber() {
        Random rand = new Random();
        int num1 = (rand.nextInt(7) + 1) * 100 + (rand.nextInt(8) * 10) + rand.nextInt(8);
        int num2 = rand.nextInt(743);
        int num3 = rand.nextInt(10000);

        DecimalFormat df3 = new DecimalFormat("000"); // 3 zeros
        DecimalFormat df4 = new DecimalFormat("0000"); // 4 zeros

        String phoneNumber = df3.format(num1) + "-" + df3.format(num2) + "-" + df4.format(num3);

        return phoneNumber;        
    }
    
   private static void addWordsIntoArray() {
        try (Scanner scanner = new Scanner(CoreFunctions.class.getClassLoader().getResourceAsStream("dictionary.txt"))) {
            while (scanner.hasNext()) {
                words.add(scanner.next());
            }
        }
    }    
}
