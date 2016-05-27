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

import com.strider.dataanonymizer.AnonymizerException;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Armenak Grigoryan
 */
public class UtilsTest extends TestCase {
    
    /**
     * Initializes logger
     */
    private static Logger log = getLogger(UtilsTest.class);    
    
    public UtilsTest(String testName) {
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
     * Test of getClassName method, of class Utils.
     * @throws com.strider.dataanonymizer.AnonymizerException
     */
    public void testGetClassName() throws AnonymizerException {
        log.info("Executing testGetClassName ...");
        
        String fullClassName = "com.strider.dataanonymizer.functions.CoreFunctions";
        String expResult = "com.strider.dataanonymizer.functions";
        
        log.debug("Parameter: " + fullClassName);
        log.debug("Expected result:" + expResult);

        String result = Utils.getClassName(fullClassName);
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getMethodName method, of class Utils.
     * @throws com.strider.dataanonymizer.AnonymizerException
     */
    public void testGetMethodName() throws AnonymizerException {
        String fullClassName = "com.strider.dataanonymizer.functions.CoreFunctions";
        String expResult = "CoreFunctions";
        
        log.debug("Parameter: " + fullClassName);
        log.debug("Expected result:" + expResult);

        String result = Utils.getMethodName(fullClassName);
        
        assertEquals(expResult, result);
    }    
}
