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



package com.strider.datadefender.database.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import com.strider.datadefender.database.DatabaseAnonymizerException;

/**
 *
 * @author Akira Matsuo
 *
 * Tests the default implementation of MetaData that's used for Oracle and MSSQL.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MetaDataTest {
    final private String schema = "test-schema";

    // private String table = "test-table";
    final private String     cType          = "test-cType";
    @SuppressWarnings("serial")
    final private Properties testProperties = new Properties() {
        {
            setProperty("schema", schema);
        }
    };
    @Mock
    private Connection mockConnection;
    @Mock
    private ResultSet  mockTableRS;

//  @Mock
//  private ResultSet mockPKRS;
    @Mock
    private ResultSet        mockColumnRS;
    @Mock
    private DatabaseMetaData mockMetaData;

    @Test
    public void testOverride() throws SQLException {
        when(mockColumnRS.getString(6)).thenReturn(cType).thenReturn(cType).thenReturn("VARCHAR");

        TestMetaData metaData = new TestMetaData(testProperties, null);

        assertEquals("No override", cType, metaData.getColumnType(mockColumnRS));
        metaData = new TestMetaData(testProperties, "Override-Strings-pls!-type");
        assertEquals("Override, but not valid string type.", cType, metaData.getColumnType(mockColumnRS));
        assertEquals("Override, with valid string type.", "String", metaData.getColumnType(mockColumnRS));
    }

    private class TestMetaData extends MetaData {
        public TestMetaData(final Properties databaseProperties, final String columnType) {
            super(databaseProperties, mockConnection);
            this.columnType = columnType;
        }
    }
}