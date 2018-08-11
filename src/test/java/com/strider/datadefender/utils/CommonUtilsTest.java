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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Akira Matsuo
 */
public class CommonUtilsTest {
    
    private static final Logger log         = getLogger(CommonUtilsTest.class);
    
    @Test
    public void isEmptyString() {
        assertTrue(CommonUtils.isEmptyString(""));
        assertTrue(CommonUtils.isEmptyString(null));
        assertFalse(CommonUtils.isEmptyString(" "));
        assertFalse(CommonUtils.isEmptyString("blah"));
    }
    
    @Test
    public void getMatchingStringTest() {
        List <String> tables = new ArrayList();
        tables.add("INTER.*");
        tables.add("TEST.*");

        List matchList = CommonUtils.getMatchingStrings(tables, "INTER_TEST".toUpperCase(Locale.ENGLISH));
        log.info(matchList.toString());
        assertTrue(matchList.size() == 1);
    }
}