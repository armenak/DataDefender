/*
 * Copyright 2014-2021, Armenak Grigoryan, and individual contributors as indicated
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

package com.strider.datadefender.utils;


import java.io.IOException;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.strider.datadefender.anonymizer.functions.Bio;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author strider
 */
@Log4j2
public class EncoderTest {

    private static String hash = null;
    
    /**
     * Static block to load hash string for "salting" the encryption
     */
    static {
        String file = System.getProperty("user.dir") + "/hash.txt";
        File f = new File(file);
        try {        
            BufferedReader reader = new BufferedReader(new FileReader(f));
            hash = reader.readLine();
            reader.close();            
        } catch (IOException ex) {
            log.error("Problem finding file hash.txt", ex);
        }
    }        
    
    
    public EncoderTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of encode method, of class Encoder.
     */
    @Test
    public void testRandomFirstNameWithParameter() {
        log.debug("Testing enocode() method");
        
        String originalValue = "Armenak Grigoryan";
        
        String encryptedValue = new Bio().randomFirstName(originalValue);
        log.debug("Encrypted value=" + encryptedValue);
        
        String decryptedValue = new Encoder().decrypt(encryptedValue, hash);
        log.debug("Decrypted value=" + decryptedValue);
        
        assertEquals(originalValue, decryptedValue);
    }
    
    @Test
    public void testRandomFirstNameWithoutParameter() {
        log.debug("Testing randomFirstName() method");
        
        String randomFirstName = "";
        try {
            randomFirstName = new Bio().randomFirstName();
        } catch (IOException ex) {
            log.error("Problem generating random first name", ex);
        }
        
        assertTrue(randomFirstName != null);
    }
    
}
