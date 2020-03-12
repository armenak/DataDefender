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

package com.strider.datadefender.functions;

import com.strider.datadefender.utils.Xeger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Armenak Grigoryan
 */
public class CoreFunctions {
    
    private static final Logger log = getLogger(CoreFunctions.class);

    private static Random rand = new Random();    
    
    private static Map<String, List<String>> stringLists = new HashMap<>();
    private static Map<String, Iterator<String>> stringIters = new HashMap<>();
    private static List<String> words = new ArrayList<>();
    private static Map<String, Map<String, String>> predictableShuffle = new HashMap<>();
    private static List<String> lipsumParagraphs = new ArrayList<>();
    
    /**
     * Set after construction with a call to setDatabaseConnection.
     */
    private Connection db;
    
    /**
     * Set current database vendor name (mysql, oracle, etc)
     */
    private String vendor;
    
    static {        
        log.debug("*** Adding list of words into array");
        addWordsIntoArray();
        log.debug("*** Array is populated with words from dictionary");
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
    public void setDatabaseConnection(final Connection db) {
        this.db = db;
    }
    
    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }
    
    /**
     * Returns a List of paragraphs loaded from the lorem ipsum text file.
     * 
     * The function separates the text into paragraphs, adding each paragraph to
     * the list and returning it.
     * 
     * @return the list of paragraphs
     * @throws IOException if an error occurs reading from the file.
     */
    private List<String> getLipsumParagraphs() throws IOException {
        if (lipsumParagraphs.isEmpty()) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(CoreFunctions.class.getClassLoader().getResourceAsStream("lipsum.txt")));
            final StringBuilder sb = new StringBuilder();
            for (String line; (line = br.readLine()) != null; ) {
                if (line.trim().length() == 0) {
                    lipsumParagraphs.add(sb.toString());
                    sb.setLength(0);
                    continue;
                }
                sb.append(line);
            }
            lipsumParagraphs.add(sb.toString());
        }
        return lipsumParagraphs;
    }
    
    /**
     * Returns the next shuffled item from the named collection.
     * 
     * @param name
     * @return 
     */
    private String getNextShuffledItemFor(final String name) {
        if (stringIters.containsKey(name)) {
            final Iterator<String> iter = stringIters.get(name);
            if (iter.hasNext()) {
                return iter.next();
            }
        }
        
        final List<String> list = stringLists.get(name);
        Collections.shuffle(list);
        
        final Iterator<String> iter = list.iterator();
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
    private String getPredictableShuffledValueFor(final String name, final String value) {
        if (!predictableShuffle.containsKey(name)) {
            final List<String> list = stringLists.get(name);
            final List<String> shuffled = new ArrayList<>(list);
            Collections.shuffle(shuffled);

            final Map<String, String> smap = new HashMap<>();
            final Iterator<String> lit = list.iterator();
            final Iterator<String> sit = shuffled.iterator();
            while (lit.hasNext()) {
                smap.put(lit.next(), sit.next());
            }
            predictableShuffle.put(name, smap);
        }
        
        final Map<String, String> map = predictableShuffle.get(name);
        if (!map.containsKey(value)) {
            final String[] vals = map.values().toArray(new String[map.size()]);
            final int index = (int) Math.abs((long) value.hashCode()) % vals.length;
            return vals[index];
        }
        return map.get(value);
    }
    
    public String generateStringFromPattern(final String regex) {
        final Xeger instance = new Xeger(regex);
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
     * @throws java.io.IOException
     */
    public String randomStringFromFile(final String file) throws IOException {
        if (!stringLists.containsKey(file)) {
            log.info("*** reading from " + file);
            final List<String> values = new ArrayList<>();
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
     * @throws java.sql.SQLException 
     */
    protected void generateStringListFromDb(final String keyName, final String query) throws SQLException {
        if (!stringLists.containsKey(keyName + query.hashCode())) {
            log.info("*** reading from database column: " + keyName);
            final List<String> values = new ArrayList<>();
            
            log.debug("Query:" + query);
            try (Statement stmt = db.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    values.add(rs.getString(1));
                }
            }
            
            if (values.isEmpty()) {
                // TODO: throw a meaningful exception here
                log.error("!!! Database column " + keyName + " did not return any values");
            }
            stringLists.put(keyName + query.hashCode(), values);
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
     * @param excludeEmpty set to true to exclude empty values
     * @return the next item
     * @throws SQLException 
     */
    public String randomColumnValue(final String table, final String column, final boolean excludeEmpty) throws SQLException {
        final String keyName = table + "." + column;
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT DISTINCT %s FROM %s", column, table));
        if (excludeEmpty) {
            if ("oracle".equals(vendor)) {
                sb.append(String.format(" WHERE %s IS NOT NULL", column, column));                
            } else {
                sb.append(String.format(" WHERE %s IS NOT NULL AND %s <> ''", column, column));       
            }
        }
        generateStringListFromDb(keyName, sb.toString());
        return getNextShuffledItemFor(keyName + sb.toString().hashCode());
    }
    
    /**
     * Generates a randomized collection of column values and selects and
     * returns one.
     * 
     * Same as calling randomColumnValue with excludeEmpty set to true (empty
     * values are excluded).  For backwards compatibility this differs from
     * the default 'randomColumnValue' exclusion policy and is therefore
     * deprecated.
     * 
     * @param table the table name
     * @param column the column name
     * @return the next item
     * @deprecated
     * @throws SQLException 
     */
    public String randomColumnValue(final String table, final String column) throws SQLException {
        return this.randomColumnValue(table, column, true);
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
     * @param excludeEmpty
     * @return
     * @throws SQLException 
     */
    public String mappedColumnShuffle(final String table, final String column, final String value, final boolean excludeEmpty) throws SQLException {
        final String keyName = table + "." + column;
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT DISTINCT %s FROM %s", column, table));
        if (excludeEmpty) {
            if ("oracle".equals(vendor)) {
                sb.append(String.format(" WHERE %s IS NOT NULL", column, column));                
            } else {
                sb.append(String.format(" WHERE %s IS NOT NULL AND %s <> ''", column, column));                
            }
        }
        generateStringListFromDb(keyName, sb.toString());
        return getPredictableShuffledValueFor(keyName + sb.toString().hashCode(), value);
    }
    
    /**
     * Returns a 'predictable' shuffled value based on the passed value which is
     * guaranteed to return the same random value for the same column value.
     * 
     * Same as calling mappedColumnShuffle with excludeEmpty set to false (empty
     * values are NOT excluded).  For backwards compatibility this differs from
     * the default 'randomColumnValue' exclusion policy and is therefore
     * deprecated.
     * 
     * @param table
     * @param column
     * @param value
     * @return
     * @deprecated 
     * @throws SQLException 
     */
    public String mappedColumnShuffle(final String table, final String column, final String value) throws SQLException {
        return this.mappedColumnShuffle(table, column, value, false);
    }
    
    public String randomFirstName(final String file) throws IOException {
		return randomStringFromFile(file);
    }
    
    public String randomLastName(final String file) throws IOException {        
        return randomStringFromFile(file);
    }
    
    public String randomMiddleName(final String file) throws IOException {        
        return randomStringFromFile(file);
    }
    
    public String randomEmail(final String domainName) {
        final StringBuilder email = new StringBuilder();
        email.append(generateRandomString(1,43).trim()).append('@').append(domainName);
        return email.toString();
    }    
    
    /**
     * Returns email address sent as a parameter 
     * @param email
     * @deprecated use setValue
     * @return email address String 
     */
    public String staticEmail(final String email) {
        return email;
    }

    /**
     * Returns the value passed as a parameter
     * @param value
     * @return value the value passed
     */
    public String setValue(final String value) {
        return value;
    }

    /**
     * Returns empty string 
     * @return "" 
     */
    public String setEmptyString() {
        return "";
    }            

    /**
     * Generates random postal code
     * @param file
     * @return String Random postal code
     * @throws IOException 
     */
    public String randomPostalCodeFromFile(final String file) throws IOException {
        return randomStringFromFile(file);
    }
    
    /**
     * Generates random postal code
     * @return String Random postal code
     * @throws IOException 
     */
    public String randomPostalCode() {
        final StringBuilder sb = new StringBuilder();
        sb.append(RandomStringUtils.randomAlphabetic(1).toUpperCase(Locale.ENGLISH)).
                append(randInt(1,9)).
                append(RandomStringUtils.randomAlphabetic(1).toUpperCase(Locale.ENGLISH)).
                append(randInt(1,9)).
                append(RandomStringUtils.randomAlphabetic(1).toUpperCase(Locale.ENGLISH)).
                append(randInt(1,9)); 
        
        log.debug("Generated postal code: " + sb.toString());
        return sb.toString();
    }    

    public String randomCity(final String file) throws IOException {
        return randomStringFromFile(file);
    }
    
    public String randomStreet(final String file) throws IOException {
        return randomStringFromFile(file);
    }    
    
    public String randomState(final String file)  throws IOException {
        return randomStringFromFile(file);
    }        

    /**
     * Generates a random date between the passed start and end dates, and using
     * the passed format to parse the dates passed, and to format the return
     * value.
     *
     * @param start
     * @param end
     * @param format
     * @return 
     */
    public String randomDate(final String start, final String end, final String format) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        LocalDate ds = LocalDate.parse(start, fmt);
        LocalDate de = LocalDate.parse(end, fmt);
        long day = RandomUtils.nextLong(0, de.toEpochDay() - ds.toEpochDay()) + ds.toEpochDay();
        return LocalDate.ofEpochDay(day).format(fmt);
    }

    /**
     * Generates a random date-time between the passed start and end dates, and
     * using the passed format to parse the dates passed, and to format the
     * return value.
     *
     * @param start
     * @param end
     * @param format
     * @return
     */
    public String randomDateTime(final String start, final String end, final String format) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        LocalDateTime ds = LocalDateTime.parse(start, fmt);
        LocalDateTime de = LocalDateTime.parse(end, fmt);
        long day =RandomUtils.nextLong(0, de.toEpochSecond(ZoneOffset.UTC) - ds.toEpochSecond(ZoneOffset.UTC)) + ds.toEpochSecond(ZoneOffset.UTC);
        return LocalDateTime.ofEpochSecond(day, 0, ZoneOffset.UTC).format(fmt);
    }

    /**
     * Generates a random string of 'num' words, with at most 'length'
     * characters (shortening the string if more characters appear in the
     * string).
     * 
     * @param num The number of desired words
     * @param length The maximum length of the string
     * @return 
     */
    public String generateRandomString(final int num, final int length) {
        final Random random = new Random();
        final StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < num && randomString.length() < length; i++) {
            final int rand = random.nextInt(479617);
            randomString.append(words.get(rand)).append(' ');
        }

        if (randomString.length() > length) {
            return randomString.toString().substring(0, length).trim();
        }
        return randomString.toString();
    }
    
    public String randomDescription(final int num, final int length) {
        return this.generateRandomString(num, length);
    }
    
    /**
     * Generates between min and max (inclusive) lorem ipsum sentences.
     * 
     * The sentences are generated from the beginning of paragraphs, although
     * the first paragraph chosen is random.  Paragraphs are joined together to
     * form sentences without line breaks.
     * 
     * @param min Minimum number of sentences to generate
     * @param max Maximum number of sentences to generate
     * @return the generated sentences.
     * @throws IOException if an error occurs reading from the lipsum text file
     */
    public String lipsumSentences(final int min, final int max) throws IOException {
        final List<String> lp = getLipsumParagraphs();
        final Random rand = new Random();
        final StringBuilder sb = new StringBuilder();
        
        final int nSentences = max - rand.nextInt((max + 1) - min);
        String separator = "";
        mainLoop:
        for (int i = 0, start = rand.nextInt(lp.size()); i < nSentences; ++start) {
            final String para = lp.get(start % lp.size());
            final String[] sentences = para.split("\\.");
            for (String s : sentences) {
                s = s.trim().replaceAll("\\s+", " ");
                if (s.isEmpty()) {
                    sb.append('.');
                    continue;
                }
                
                sb.append(separator).append(s).append('.');
                separator = " ";
                ++i;
                
                if (i == nSentences) {
                    break mainLoop;
                }
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generates the specified number of paragraphs.
     * 
     * The paragraphs are generated from the loaded lorem ipsum text.  The start
     * position of the text is randomized, however the following paragraphs
     * appear in sequence - restarting at the beginning if all paragraphs have
     * been used.
     * 
     * @param paragraphs the number of paragraphs to generate
     * @return the paragraphs
     * @throws IOException if an error occurs reading from the lipsum text file
     */
    public String lipsumParagraphs(final int paragraphs) throws IOException {
        final List<String> lp = getLipsumParagraphs();
        final Random rand = new Random();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, start = rand.nextInt(lp.size()); i < paragraphs; ++i, ++start) {
            sb.append(lp.get(start % lp.size())).append("\r\n\r\n");
        }
        return sb.toString().trim();
    }
    
    /**
     * Generates text similar to the text passed as a parameter.
     * 
     * The method counts the number of paragraphs in the text, generating the
     * same number of "lorem ipsum" paragraphs.  If the text doesn't contain
     * paragraphs, the method counts the number of sentences and generates a
     * similar amount of sentences (+/- 1 sentence).
     * 
     * @param text the text to use as a basis for generation
     * @return the generated lorem ipsum text
     * @throws IOException if an error occurs reading from the lipsum text file
     */
    public String lipsumSimilar(final String text) throws IOException {
        final String sParas = text.replaceAll("\r\n", "\n");
        final int nParas = StringUtils.countMatches(sParas, "\n");
        if (nParas > 0) {
            final StringBuilder sb = new StringBuilder();
            for (final String para : sParas.split("\n")) {
                if (para.trim().isEmpty()) {
                    sb.append("\r\n");
                    continue;
                }
                sb.append(lipsumSimilar(para)).append("\r\n");
            }
            return sb.toString().trim();
        }
        final int nSent = StringUtils.countMatches(text.replaceAll("\\.{2,}", "."), ".");
        return lipsumSentences(Math.max(1, nSent - 1), Math.max(1, nSent + 1));
    }

    /**
     * Replaces all occurrences of 'find' in the passed text with the passed
     * 'replace' parameter.
     *
     * @param text
     * @param find
     * @param replacement
     * @return the replaced string
     */
    public String replace(final String text, final String find, final String replacement) {
        return text.replace(find, replacement);
    }

    /**
     * Effectively calls String.replaceAll on the passed text, with the given
     * regex pattern and replacement parameters.
     *
     * @param text
     * @param regex
     * @param replacement
     * @return the replaced string
     */
    public String regexReplace(final String text, final String regex, final String replacement) {
        return text.replaceAll(regex, replacement);
    }
 
    public String randomPhoneNumber() {
        final Random rand = new Random();
        final int num1 = (rand.nextInt(7) + 1) * 100 + (rand.nextInt(8) * 10) + rand.nextInt(8);
        final int num2 = rand.nextInt(743);
        final int num3 = rand.nextInt(10000);

        final DecimalFormat df3 = new DecimalFormat("000"); // 3 zeros
        final DecimalFormat df4 = new DecimalFormat("0000"); // 4 zeros

        final String phoneNumber = df3.format(num1) + "-" + df3.format(num2) + "-" + df4.format(num3);

        return phoneNumber;        
    }
    
   private static void addWordsIntoArray() {
        try (Scanner scanner = new Scanner(CoreFunctions.class.getClassLoader().getResourceAsStream("dictionary.txt"))) {
            while (scanner.hasNext()) {
                words.add(scanner.next());
            }
        }
    }
   
    /**
     * Returns a pseudo-random number between min and max, inclusive.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(final int min, final int max) {

        rand = new Random();
        final int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
