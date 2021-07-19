/*
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
 */
package com.strider.datadefender.anonymizer.functions;

import com.strider.datadefender.functions.NamedParameter;
import com.strider.datadefender.requirement.registry.RequirementFunction;
import com.strider.datadefender.utils.Xeger;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.RandomUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Core set of helpers for data anonymization.
 *
 * @author Armenak Grigoryan
 */
@Log4j2
public class Core extends RequirementFunction {

    private static final Map<String, List<String>> stringLists = new HashMap<>();
    private static final Map<String, Iterator<String>> stringIters = new HashMap<>();
    private static final  List<String> words = new ArrayList<>();
    private static String hash = null;
    
    public static interface InputStreamSupplier {
        public InputStream getInputStream() throws IOException;
    }

    /**
     * Static block for loading words from dictionary
     */
    static {
        try (Scanner scanner = new Scanner(Core.class.getResourceAsStream("dictionary.txt"))) {
            while (scanner.hasNext()) {
                words.add(scanner.next());
            }
        }
    }
    
    /**
     * Static block to load hash string for "salting" the encryption
     */
    static {
        try (Scanner scanner = new Scanner(Core.class.getResourceAsStream("hash.txt"))) {
            if (scanner.hasNext()) {
                hash = scanner.next();
            }
        }
    }    

    /**
     * Returns the next shuffled item from the named collection.
     *
     * @param String name
     * @return String
     */
    private String getNextShuffledItemFor(@NamedParameter("name") String name) {
        if (stringIters.containsKey(name)) {
            final Iterator<String> iter = stringIters.get(name);
            if (iter.hasNext()) {
                return iter.next();
            }
            // else shuffle again
        }

        final List<String> list = stringLists.get(name);
        Collections.shuffle(list);

        final Iterator<String> iter = list.iterator();
        stringIters.put(name, iter);
        return iter.next();
    }

    /**
     * Generates a list of random strings from a list of strings (new-
     * line separated) in a file.The function randomizes the collection, exhausting all possible values
     * before re-shuffling and re-using items.

     * @param id
     * @param supplier
     * @return A random string from the stream
     * @throws IOException
     */
    protected String randomStringFromStream(String id, InputStreamSupplier supplier) throws IOException {
        if (!stringLists.containsKey(id)) {
            log.info("Loading words from stream {}", id);
            final List<String> values = new ArrayList<>();
            InputStream stream = supplier.getInputStream();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                for (String line; (line = br.readLine()) != null; ) {
                    values.add(line);
                }
            }
            stringLists.put(id, values);
        }
        return getNextShuffledItemFor(id);
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
    public String randomStringFromFile(String file) throws IOException {
        if (!stringLists.containsKey(file)) {
            log.info("Loading words from file: {}", file);
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
     * Generates a random date between the passed start and end dates, and using
     * the passed format to parse the dates passed, and to format the return
     * value.
     *
     * @param start
     * @param end
     * @param format
     * @return
     */
    public String randomDate(
        @NamedParameter("start") String start,
        @NamedParameter("end") String end,
        @NamedParameter("format") String format
    ) {
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
    public String randomDateTime(
        @NamedParameter("start") String start,
        @NamedParameter("end") String end,
        @NamedParameter("format") String format
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        LocalDateTime ds = LocalDateTime.parse(start, fmt);
        LocalDateTime de = LocalDateTime.parse(end, fmt);
        long day = RandomUtils.nextLong(0, de.toEpochSecond(ZoneOffset.UTC) - ds.toEpochSecond(ZoneOffset.UTC)) + ds.toEpochSecond(ZoneOffset.UTC);
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
    public String randomString(@NamedParameter("num") int num, @NamedParameter("length") int length) {
        final StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < num && randomString.length() < length; ++i) {
            final int r = RandomUtils.nextInt(0, words.size());
            randomString.append(words.get(r)).append(' ');
        }
        if (randomString.length() > length) {
            return randomString.toString().substring(0, length).trim();
        }
        return randomString.toString().trim();
    }

    /**
     * Generates a String from the passed regex that is guaranteed to match it.
     *
     * @param pattern
     * @return
     */
    public String randomStringFromPattern(@NamedParameter("pattern") String pattern) {
        final Xeger instance = new Xeger(pattern);
        return instance.generate();
    }
    
    public String randomIpAddress() {
        
        StringBuilder ipAddress = new StringBuilder();
        Random r = new Random();
        
        ipAddress.append(Integer.toString(r.nextInt(256))).
                append(".").
                append(Integer.toString(r.nextInt(256))).
                append(".").
                append(r.nextInt(256)).
                append(".").
                append(r.nextInt(256));
        
        
        return ipAddress.toString();
    }
    
    public String randomGeoLocation() {
        double val = new Random().nextDouble();
        return Double.toString(val);
    }    
    
    public String getHash() {
        return hash;
    }
    
    
}
