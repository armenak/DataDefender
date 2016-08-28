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
 *
 */
package com.strider.datadefender;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

/**
 * @author Akira Matsuo
 */
public class AnonymizerTest {
    
    @SuppressWarnings("serial")
    @Test
    public void testGetTableNames() throws AnonymizerException {
        String t1 = "tableOne";
        List<String> tableList = Arrays.asList(t1);
        Set<String> tableNames = DataDefender.getTableNames(tableList, new Properties());
        assertEquals(1, tableNames.size());
        assertEquals("Handle table args.", t1.toLowerCase(), tableNames.iterator().next());
        
        Properties overrideProps = new Properties() {{
            setProperty("tables", "oneT twoT");
        }};
        tableNames = DataDefender.getTableNames(tableList, overrideProps);
        assertEquals(1, tableNames.size());
        assertEquals("Ignore props", t1.toLowerCase(), tableNames.iterator().next());
        
        tableNames = DataDefender.getTableNames(Collections.emptyList(), overrideProps);
        assertEquals(2, tableNames.size());
        assertArrayEquals("Props from file", new String[] {"onet", "twot"}, tableNames.toArray());
    }
}
