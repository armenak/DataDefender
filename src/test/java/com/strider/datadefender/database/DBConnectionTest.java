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
package com.strider.datadefender.database;

import com.strider.datadefender.database.DatabaseAnonymizerException;
import com.strider.datadefender.database.DBConnection;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Using mock to test Connection.
 * @author Akira Matsuo
 */
@RunWith(MockitoJUnitRunner.class)  
public class DBConnectionTest {
    @SuppressWarnings("serial")
    private Properties testProps = new Properties() {{
        setProperty("vendor", "mysql");
        setProperty("driver", "java.util.List");
        setProperty("url", "invalid-url");
        setProperty("username", "invalid-user");
        setProperty("password", "invalid-pass");
    }};
    @Mock  
    private Connection mockConnection; 
    // testing class
    private class TestDBConnection extends DBConnection {
        public TestDBConnection(Properties properties) throws DatabaseAnonymizerException {
            super(properties);
        }
        @Override
        public Connection connect() throws DatabaseAnonymizerException {
            return doConnect(() -> {
                this.runAsserts();
                return mockConnection;
            });
        }
        public void runAsserts() {
            assertEquals("mysql", this.vendor);
            assertEquals("java.util.List", this.driver);
            assertEquals("invalid-url", this.url);
            assertEquals("invalid-user", this.userName);
            assertEquals("invalid-pass", this.password);
        }
    }

    @Test
    public void testCtor() throws DatabaseAnonymizerException {
        TestDBConnection testDB = new TestDBConnection(testProps);
        testDB.runAsserts();
    }
    
    @Test
    public void testConnect() throws DatabaseAnonymizerException, SQLException {
        TestDBConnection testDB = new TestDBConnection(testProps);
        assertEquals(mockConnection, testDB.connect());
        // assert
        verify(mockConnection).setAutoCommit(false);
    }
}
