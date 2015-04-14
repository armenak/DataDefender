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
package com.strider.dataanonymizer;

import static org.junit.Assert.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;

import org.junit.Test;

import com.strider.dataanonymizer.database.H2DB;

/**
 * @author Akira Matsuo
 */
public class DatabaseAnonymizerTest extends H2DB {
    
    @SuppressWarnings("serial")
    final Properties sampleAProps = new Properties() {{ 
        setProperty("batch_size", "10" ); 
        setProperty("requirement", "target/test-classes/Requirement-H2DB.xml");
    }};

    private void printLine(ResultSet rs) throws SQLException {
        System.out.println("Print Line...");
        while (rs.next()) {
            System.out.println(rs.getInt("id") + ": " + rs.getString("fname") + ", " + rs.getString("lname"));
        }
    }
    
    @Test
    public void testHappyPath() throws AnonymizerException, SQLException { 
        consumeQuery(this::assertInitialData);
        
        IAnonymizer anonymizer = new DatabaseAnonymizer();
        anonymizer.anonymize(factory, sampleAProps, new HashSet<String>());
        
        consumeQuery(this::printLine);
        consumeQuery((rs) -> { // simple checks to see that data has been changed
            assertData(rs, (notExpected, actual) -> { 
                assertNotEquals(notExpected + " : " + actual, notExpected, actual);
                assertTrue(actual, actual.matches(".*(h2db|oracle|mysql).*"));
            });
        });
    }
}
