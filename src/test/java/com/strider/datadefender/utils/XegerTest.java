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

package com.strider.datadefender.utils;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author strider
 */
public class XegerTest extends TestCase {
    private static final Logger log = getLogger(XegerTest.class);
    
    private static final String REGEXP_1 = "[ab]{4,6}c";
    private static final String REGEXP_2 = "[0-9]{3}-[0-9]{3}-[0-9]{3}";

    
    public XegerTest(final String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of generate method, of class Xeger.
     */
    public void testGenerate() {
        log.debug("Generate string");
        final Xeger instance = new Xeger(REGEXP_1);
        
        for (int i = 0; i < 100; i++) {
            final String text = instance.generate();
            assertTrue(text.matches(REGEXP_1));
        }
    }
    
    /**
     * Test of generate method, of class Xeger.
     */
    public void testGenerateSIN() {
        log.debug("Generate SIN");
        
        final Xeger instance = new Xeger(REGEXP_2);
        final String text = instance.generate();
        log.debug(text);
        assertTrue(text.matches(REGEXP_2));
    }    
    
}
