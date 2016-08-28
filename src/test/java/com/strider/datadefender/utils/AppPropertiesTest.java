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

import static com.strider.datadefender.utils.AppProperties.loadProperties;
import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import com.strider.datadefender.AnonymizerException;


/**
* @author Akira Matsuo
*/
public class AppPropertiesTest {
    
    @Test(expected=AnonymizerException.class)
    public void testLoadPropertiesNoFile() throws AnonymizerException {
        loadProperties("/do/not/exist.properties");
    }

    @Test
    public void testLoadPropertiesValid() throws AnonymizerException {
        String path = this.getClass().getClassLoader().getResource("AppPropertiesTest.properties").getPath();
        Properties props = loadProperties(path);
        assertNotNull(props);
        assertEquals("yyy", props.get("xxx"));
    }
}
