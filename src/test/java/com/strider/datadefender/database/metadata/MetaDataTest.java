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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    @Mock
    private Connection mockConnection;
    @Mock
    private ResultSet mockTableRS;
//    @Mock
//    private ResultSet mockPKRS;
    @Mock
    private ResultSet mockColumnRS;
    @Mock
    private DatabaseMetaData mockMetaData;
    
    final private String schema = "test-schema";
    //private String table = "test-table";
    final private String cType = "test-cType";
    
    @SuppressWarnings("serial")
    final private Properties testProperties = new Properties() {{
        setProperty("schema", schema);
    }};
    
    private class TestMetaData extends MetaData {
        public TestMetaData(final Properties databaseProperties, final String columnType) {
            super(databaseProperties, mockConnection);
            this.columnType = columnType;
        }
    }

    @Test
    public void testOverride() throws SQLException {
        when(mockColumnRS.getString(6)).thenReturn(cType).thenReturn(cType).thenReturn("VARCHAR");
        
        TestMetaData metaData = new TestMetaData(testProperties, null);
        assertEquals("No override", cType, metaData.getColumnType(mockColumnRS));
        metaData = new TestMetaData(testProperties, "Override-Strings-pls!-type");
        assertEquals("Override, but not valid string type.", cType, metaData.getColumnType(mockColumnRS));
        assertEquals("Override, with valid string type.", "String", metaData.getColumnType(mockColumnRS));
    }
    
    @Test
    public void testNoTables() throws DatabaseAnonymizerException, SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, schema, null, new String[] {"TABLE"})).thenReturn(mockTableRS);
        
        TestMetaData metaData = new TestMetaData(testProperties, null);
        List<MatchMetaData> result = metaData.getMetaData();
        assertEquals(0, result.size());
        
        verify(mockTableRS, times(1)).next(); // should return false
        verify(mockTableRS, times(1)).close();
    }
    
//    @Test
//    public void testNoColumns() throws DatabaseAnonymizerException, SQLException {
//        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
//        when(mockMetaData.getTables(null, schema, null, new String[] {"TABLE"})).thenReturn(mockTableRS);
//        when(mockMetaData.getColumns(null, schema, table, null)).thenReturn(mockColumnRS);
//        when(mockMetaData.getPrimaryKeys(null, schema, table)).thenReturn(mockPKRS);
//        when(mockTableRS.getString(3)).thenReturn(table);
//        when(mockTableRS.next()).thenReturn(true).thenReturn(false);
//        
//        TestMetaData metaData = new TestMetaData(testProperties, null);
//        List<MatchMetaData> result = metaData.getMetaData();
//        assertEquals(0, result.size());
//        
//        verify(mockColumnRS, times(1)).next(); // returns false
//        verify(mockTableRS, times(1)).close();
//        verify(mockColumnRS, times(1)).close();
//    }
    
//    @Test
//    public void testHappyPath() throws DatabaseAnonymizerException, SQLException {
//        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
//        when(mockMetaData.getTables(null, schema, null, new String[] {"TABLE"})).thenReturn(mockTableRS);
//        when(mockMetaData.getColumns(null, schema, table, null)).thenReturn(mockColumnRS);
//        when(mockTableRS.getString(3)).thenReturn(table);
//        when(mockTableRS.next()).thenReturn(true).thenReturn(false); // just one element
//        when(mockMetaData.getPrimaryKeys(null, schema, table)).thenReturn(mockPKRS);
//        when(mockColumnRS.next()).thenReturn(true).thenReturn(false);
//        when(mockColumnRS.getString(4)).thenReturn("cName");
//        when(mockColumnRS.getString(6)).thenReturn(cType);
//                
//        TestMetaData metaData = new TestMetaData(testProperties, null);
//        List<MatchMetaData> result = metaData.getMetaData();
//        assertEquals(1, result.size());
//        assertEquals(table, result.get(0).getTableName());
//        assertEquals("cName", result.get(0).getColumnName());
//        assertEquals(cType, result.get(0).getColumnType());
//        
//        verify(mockTableRS, times(1)).close();
//        verify(mockColumnRS, times(1)).close();
//    }
    
//    @Test
//    public void testExceptionThrown() throws DatabaseAnonymizerException, SQLException {
//        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
//        when(mockMetaData.getTables(null, schema, null, new String[] {"TABLE"})).thenReturn(mockTableRS);
//        when(mockMetaData.getColumns(null, schema, table, null)).thenReturn(mockColumnRS);
//        when(mockTableRS.getString(3)).thenReturn(table);
//        when(mockTableRS.next()).thenReturn(true).thenReturn(false); // just one element
//        when(mockMetaData.getPrimaryKeys(null, schema, table)).thenReturn(mockPKRS);
//        when(mockColumnRS.next()).thenThrow(new SQLException());
//        
//        TestMetaData metaData = new TestMetaData(testProperties, null);
//        List<MatchMetaData> result = metaData.getMetaData();
//        assertEquals(0, result.size());
//        
//        verify(mockTableRS, times(1)).close();
//        verify(mockColumnRS, times(1)).close();
//    }
}
