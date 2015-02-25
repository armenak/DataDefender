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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Armenak Grigoryan
 */
public class CoreFunctions {
    
    private static final Logger log = getLogger(CoreFunctions.class);

    private static Map<String, List<String>> stringLists = new HashMap<>();
    private static Map<String, Iterator<String>> stringIters = new HashMap<>();
    private static List<String> words = new ArrayList<>();
    private static Map<String, Map<String, String>> predictableShuffle = new HashMap<>();
    
    /**
     * Set after construction with a call to setDatabaseConnection.
     */
    private Connection db;

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
    
    /**
     * Passes the active database connection.
     * 
     * The database connection is available in the protected db member variable.
     * CoreFunctions uses the connection to generate random strings from data in
     * the database.
     * 
     * @param db the active database connection
     */
    public void setDatabaseConnection(Connection db) {
        this.db = db;
    }
    
    /**
     * Returns the next shuffled item from the named collection.
     * 
     * @param name
     * @return 
     */
    private String getNextShuffledItemFor(String name) {
        if (stringIters.containsKey(name)) {
            Iterator<String> iter = stringIters.get(name);
            if (iter.hasNext()) {
                return iter.next();
            }
        }
        
        List<String> list = stringLists.get(name);
        Collections.shuffle(list);
        
        Iterator<String> iter = list.iterator();
        stringIters.put(name, iter);
        return iter.next();
    }
    
    /**
     * Sets up a map, mapping a list of values to a list of shuffled values.
     * 
     * If the value is not mapped, the function guarantees returning the same
     * randomized value for a given column value - however it does not guarantee
     * that more than one column value do not have the same randomized value.
     * 
     * @param params
     * @return 
     */
    private String getPredictableShuffledValueFor(String name, String value) {
        if (!predictableShuffle.containsKey(name)) {
            List<String> list = stringLists.get(name);
            List<String> shuffled = new ArrayList<String>(list);
            Collections.shuffle(shuffled);
            
            Map<String, String> smap = new HashMap<>();
            Iterator<String> lit = list.iterator();
            Iterator<String> sit = shuffled.iterator();
            while (lit.hasNext()) {
                smap.put(lit.next(), sit.next());
            }
            predictableShuffle.put(name, smap);
        }
        
        Map<String, String> map = predictableShuffle.get(name);
        if (!map.containsKey(value)) {
            String[] vals = map.values().toArray(new String[map.size()]);
            int index = (int) Math.abs((long) value.hashCode()) % vals.length;
            return vals[index];
        }
        return map.get(value);
    }
    
    public String generateStringFromPattern(String regex) {
        Xeger instance = new Xeger(regex);
        return instance.generate();
    }
    
    /**
     * Generates a list of random strings from a list of strings (new-
     * line separated) in a file.
     * 
     * The function randomizes the collection, exhausting all possible values
     * before re-shuffling and re-using items.
     * 
     * @param file the file name
     * @return A random string from the file
     */
    public String randomStringFromFile(String file) throws IOException {
		if (!stringLists.containsKey(file)) {
			log.info("*** reading from " + file);
			List<String> values = new ArrayList<String>();
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line; (line = br.readLine()) != null; ) {
                    values.add(line);
                }
            }
            stringLists.put(file, values);
		}
		
        return getNextShuffledItemFor(file);
	}
    
    /**
     * Creates a string list of values by querying the database.
     * 
     * @param keyName
     * @param query
     * @return 
     */
    protected void generateStringListFromDb(String keyName, String query) throws SQLException {
        if (!stringLists.containsKey(keyName)) {
            log.info("*** reading from database column: " + keyName);
			List<String> values = new ArrayList<String>();
            
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                values.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            
            stringLists.put(keyName, values);
        }
    }
    
    /**
     * Generates a randomized collection of column values and selects and
     * returns one.
     * 
     * The function selects distinct, non-null and non-empty values to choose
     * from, then shuffles the collection of strings once before returning items
     * from it.  Once all strings have been returned, the collection is
     * re-shuffled and re-used.
     * 
     * @param table the table name
     * @param column the column name
     * @return the next item
     * @throws SQLException 
     */
    public String randomColumnValue(String table, String column) throws SQLException {
        String keyName = table + "." + column;
        generateStringListFromDb(keyName, String.format("SELECT DISTINCT %s FROM %s WHERE %s IS NOT NULL AND %s <> ''", column, table, column, column));
        return getNextShuffledItemFor(keyName);
    }
    
    /**
     * Returns a 'predictable' shuffled value based on the passed value which is
     * guaranteed to return the same random value for the same column value.
     * 
     * Note that more than one column value may result in having the same
     * shuffled value.
     * 
     * @param table
     * @param column
     * @param value
     * @return
     * @throws SQLException 
     */
    public String mappedColumnShuffle(String table, String column, String value) throws SQLException {
        String keyName = table + "." + column;
        generateStringListFromDb(keyName, String.format("SELECT DISTINCT %s FROM %s", column, table));
        return getPredictableShuffledValueFor(keyName, value);
    }
    
    public String randomFirstName(String file) throws IOException {
		return randomStringFromFile(file);
    }
    
    public String randomLastName(String file) throws IOException {        
        return randomStringFromFile(file);
    }    
    
    public String randomMiddleName(String file) throws IOException {        
        return randomStringFromFile(file);
    }        
    
    public String randomEmail(String domainName) {
        StringBuilder email = new StringBuilder();
        email.append(generateRandomString(1,43).trim()).append("@").append(domainName);
        return email.toString();
    }    
    
    /**
     * Returns email address sent as a parameter 
     * @param params
     * @return email address String 
     */
    public String staticEmail(String email) {
        return email;
    }        

    /**
     * Generates random postal code
     * @param params
     * @return String Random postal code
     * @throws IOException 
     */
    public String randomPostalCode(String file) throws IOException {
        return randomStringFromFile(file);
    }

    public String randomCity(String file) throws IOException {
        return randomStringFromFile(file);
    }
    
    public String randomStreet(String file) throws IOException {
        return randomStringFromFile(file);
    }    
    
    public String randomState(String file)  throws IOException {
        return randomStringFromFile(file);
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
        
        return randomString.toString().substring(0, stringLength).trim();
    }
    
    public String randomDescription(int num, int length) {
        return this.generateRandomString(num, length);
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
