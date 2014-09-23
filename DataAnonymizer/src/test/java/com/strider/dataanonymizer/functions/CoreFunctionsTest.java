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
import com.strider.dataanonymizer.utils.XegerTest;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * Core data anonymizer functions
 * 
 * @author Armenak Grigoryan
 */
public class CoreFunctionsTest extends TestCase {
    
    private static Logger log = getLogger(XegerTest.class);
    
    public CoreFunctionsTest(String testName) {
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
     * Test of generateStringFromPattern method, of class CoreFunctions.
     */
    public void testGenerateStringFromPattern() {
        log.debug("Generate SIN");
        String regex = "[0-9]{3}-[0-9]{3}-[0-9]{3}";
        Xeger instance = new Xeger(regex);
        String text = instance.generate();
        log.debug(text);
        assertTrue(text.matches(regex));
    }
}
