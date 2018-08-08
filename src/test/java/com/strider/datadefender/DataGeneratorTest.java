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

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.strider.datadefender.database.H2DB;

/**
 * @author Akira Matsuo
 */
public class DataGeneratorTest extends H2DB {
    
    private static final String TEST_DIR =  "target/test-classes";
    private static final String F_PATTERN = "dg-names";
    
    @SuppressWarnings("serial")
    private final Properties sampleAProps = new Properties() {{ 
        setProperty("requirement", TEST_DIR + "/Requirement-H2DB-DG.xml");
    }};
    
    @BeforeClass
    public static void globalSetUp() { // clean out files from last run
        Arrays.stream(new File(TEST_DIR).listFiles((d, fname) -> fname.startsWith(F_PATTERN)))
            .forEach(f -> f.delete());
    }
    @Test
    public void testHappyPath() throws DatabaseAnonymizerException, DatabaseDiscoveryException, SQLException, IOException { 
        consumeQuery(this::assertInitialData);
        
        final IGenerator generator = new DataGenerator();
        generator.generate(factory, sampleAProps);

        // dg-names.txt will contain last names
        List<String> lines = Files.readAllLines(Paths.get(TEST_DIR + "/" + F_PATTERN + ".txt"));
        assertArrayEquals(new String[] { "Bravo", "Bernasconi" }, lines.toArray());
        // dg-names.txt.bak-1 contains first names
        lines = Files.readAllLines(Paths.get(TEST_DIR + "/" + F_PATTERN + ".txt.bak-1"));
        assertArrayEquals(new String[] { "Claudio", "Ugo" }, lines.toArray());
    }
}
