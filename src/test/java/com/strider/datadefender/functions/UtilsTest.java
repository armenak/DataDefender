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
package com.strider.datadefender.functions;

import com.strider.datadefender.anonymizer.DatabaseAnonymizerException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Armenak Grigoryan
 */
public class UtilsTest {

    /**
     * Initializes logger
     */
    private final String FULL_CLASS_NAME    = "com.strider.datadefender.anonymizer.functions.Core";
    private final String EXPECTED_RESULTS_1 = "com.strider.datadefender.functions.functions";
    private final String EXPECTED_RESULTS_2 = "Core";

    @Test
    public void testGetClassName() throws Exception {
        System.out.println("Executing testGetClassName...");
        final String result = Utils.getClassName(FULL_CLASS_NAME);
        assertEquals(EXPECTED_RESULTS_1, result);
    }

    @Test
    public void testGetMethodName() throws DatabaseAnonymizerException {
        System.out.println("Executing testGetMethodName...");
        final String result = Utils.getMethodName(FULL_CLASS_NAME);
        assertEquals(EXPECTED_RESULTS_2, result);
    }
}
