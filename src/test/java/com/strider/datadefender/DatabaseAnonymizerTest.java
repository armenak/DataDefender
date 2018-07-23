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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;

import org.junit.Test;

import com.strider.datadefender.database.H2DB;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Akira Matsuo
 */
public class DatabaseAnonymizerTest extends H2DB {
    
    private static final Logger log = getLogger(DatabaseAnonymizerTest.class);

    
    @SuppressWarnings("serial")
    private final Properties sampleAProps = new Properties() {{ 
        setProperty("batch_size", "10" ); 
        setProperty("requirement", "target/test-classes/Requirement-H2DB.xml");
    }};

    private void printLine(final ResultSet rs) throws SQLException {
        log.debug("Print Line...");
        while (rs.next()) {
            log.debug(rs.getInt("id") + ": " + rs.getString("fname") + ", " + rs.getString("lname"));
        }
    }
    // Note: if this test fails w/ NoSuchMethodException and running in an IDE ensure that the -parameter is passed to compiler
    //       in eclipse it's under java compiler->classfile generation->store information about parameters
//    @Test
//    public void testHappyPath() throws AnonymizerException, SQLException { 
//        consumeQuery(this::assertInitialData);
//        
//        final IAnonymizer anonymizer = new DatabaseAnonymizer();
//        anonymizer.anonymize(factory, sampleAProps, new HashSet<String>());
//        
//        consumeQuery(this::printLine);
//        consumeQuery((rs) -> { // simple checks to see that data has been changed
//            assertData(rs, (notExpected, actual) -> { 
//                assertNotEquals(notExpected + " : " + actual, notExpected, actual);
//                assertTrue(actual, actual.matches(".*(h2db|oracle|mysql).*"));
//            });
//        });
//    }
}
