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

import java.lang.reflect.Field;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Properties;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.apache.log4j.Logger.getLogger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.utils.ISupplierWithException;

/**
 * Using mock to test Connection.
 * @author Akira Matsuo
 */
@RunWith(MockitoJUnitRunner.class)
public class MSSQLDBConnectionTest {
    private static final Logger     log       = getLogger(MSSQLDBConnectionTest.class);
    @SuppressWarnings("serial")
    final static private Properties testProps = new Properties() {
        {
            setProperty("vendor", "mssql");
            setProperty("driver", "java.util.List");
            setProperty("url", "invalid-url");
            setProperty("username", "invalid-user");
            setProperty("password", "invalid-pass");
        }
    };
    @Mock
    private Connection mockConnection;

    @Test
    public void testConnect() throws DatabaseException, DataDefenderException, SQLException {
        final TestMSSQLDBConnection testDB = new TestMSSQLDBConnection(testProps);

        assertEquals(mockConnection, testDB.connect());
        assertEquals("invalid-url;user=invalid-user;password=invalid-pass", testDB.getURL());
    }

    // testing class
    private class TestMSSQLDBConnection extends MsSqlDbConnection {
        public TestMSSQLDBConnection(final Properties properties)
                throws DatabaseException, DataDefenderException {
            super(properties);
        }

        @Override
        protected Connection doConnect(final ISupplierWithException<Connection, SQLException> supplier)
                throws DatabaseException {
            final Field[] allFields = supplier.getClass().getDeclaredFields();

            assertEquals(1, allFields.length);

            final Field field = allFields[0];

            field.setAccessible(true);

            try {    // not exactly a great test, but checks that supplier has parent's properties at least
                final String representation = ReflectionToStringBuilder.toString(field.get(supplier));

                log.debug(representation);
                assertTrue(
                    representation.contains(
                        "[driver=java.util.List,vendor=mssql,url=invalid-url,userName=invalid-user,password=invalid-pass]"));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e.toString());
            }

            return mockConnection;
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
