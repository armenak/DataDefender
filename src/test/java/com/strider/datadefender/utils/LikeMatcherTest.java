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
 * @author Zaahid Bateson
 */
public class LikeMatcherTest extends TestCase {
    private static Logger log = getLogger(LikeMatcherTest.class);
    
    public void testMatchingAtEnd() {
        log.debug("Testing match at end of string");
        final LikeMatcher matcher = new LikeMatcher("za%");
        assertTrue(matcher.matches("Zaahid"));
        assertTrue(matcher.matches("ZAA"));
        assertTrue(matcher.matches("za"));
        assertFalse(matcher.matches("Not Zaahid"));
        assertFalse(matcher.matches("Some Za"));
    }
    
    public void testMatchingAtBeginning() {
        log.debug("Testing match begginning of string");
        final LikeMatcher matcher = new LikeMatcher("%hid");
        assertTrue(matcher.matches("ZaahiD"));
        assertTrue(matcher.matches("aaHID"));
        assertTrue(matcher.matches("HID"));
        assertFalse(matcher.matches("Zaahid is not here"));
        assertFalse(matcher.matches("hiding away"));
    }
    
    public void testMultiMatcher() {
        log.debug("Testing match with multiple %'s");
        final LikeMatcher matcher = new LikeMatcher("Z%h%d");
        assertTrue(matcher.matches("ZaahiD"));
        assertTrue(matcher.matches("ZaHID"));
        assertTrue(matcher.matches("Zhd"));
        assertFalse(matcher.matches("Zaahid is not here"));
        assertFalse(matcher.matches("Someone is hiding Zaahid"));
    }
    
    public void testSingleCharMatcher() {
        log.debug("Testing match with '?' and '_'");
        final LikeMatcher matcher = new LikeMatcher("Z_?h?d");
        assertTrue(matcher.matches("ZaahiD"));
        assertFalse(matcher.matches("ZaHID"));
        assertFalse(matcher.matches("Zhd"));
        assertTrue(matcher.matches("Zaphod"));
    }
    
    public void testMixedMatcher() {
        log.debug("Testing match with a mix of '%', '?', and '_'");
        final LikeMatcher matcher = new LikeMatcher("%Z_?h?d%");
        assertTrue(matcher.matches("ZaahiD"));
        assertFalse(matcher.matches("ZaHID"));
        assertFalse(matcher.matches("Zhd"));
        assertTrue(matcher.matches("Zaphod"));
        assertTrue(matcher.matches("Zaahid really is Zaphod"));
        assertTrue(matcher.matches("Yes, Zaahid is here"));
    }
}
