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


import com.strider.datadefender.extensions.BiographicFunctions;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import static org.apache.log4j.Logger.getLogger;

/**
 * Biographic data anonymizer functions
 * 
 * @author Matthew Eaton
 */
public class BiographicFunctionsTest extends TestCase {

    private static Logger log = getLogger(BiographicFunctionsTest.class);

    public BiographicFunctionsTest(final String testName) {
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
     * Test of randomSIN method
     */
    public void testRandomSIN() {
        log.debug("Generate SIN");

        final BiographicFunctions functions = new BiographicFunctions();
        final String sin = functions.randomSIN();

        log.debug("Random SIN = " + sin);

        assertNotNull(sin);
        assertTrue(sin.length() == 9);
        assertTrue(functions.isValidSIN(sin));

    }
    
    /**
     * Test of randomBirthDate method
     */
//    public void testRandomBirthDate() throws ParseException {
//        log.debug("Generate BirthDate");
//
//        BiographicFunctions functions = new BiographicFunctions();
//        Date birthDate = functions.randomBirthDate();
//
//        log.debug("Random birthdate = " + birthDate);
//
//        assertNotNull(birthDate);
//    }    
}
