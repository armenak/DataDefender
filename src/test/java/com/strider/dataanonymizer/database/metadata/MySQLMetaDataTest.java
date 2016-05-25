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

package com.strider.dataanonymizer.database.metadata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;

/**
 * 
 * @author Akira Matsuo
 * 
 * Sanity check for MySQL impl.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MySQLMetaDataTest {
    @Mock
    private Connection mockConnection;
    @Mock
    private ResultSet mockTableRS;
    @Mock
    private ResultSet mockPKRS;
    @Mock
    private ResultSet mockColumnRS;
    @Mock
    private DatabaseMetaData mockMetaData;
    
    private String table = "test-table";
    private Properties testProperties = new Properties();
    
    private class TestMSQLMetaData extends MySQLMetaData {
        public TestMSQLMetaData(Properties databaseProperties) {
            super(databaseProperties, mockConnection);
        }
    }
    
    @Test
    public void testHappyPath() throws DatabaseAnonymizerException, SQLException {
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, "%", null)).thenReturn(mockTableRS);
        when(mockMetaData.getPrimaryKeys(null, null, table)).thenReturn(mockPKRS);
        when(mockMetaData.getColumns(null, null, table, null)).thenReturn(mockColumnRS);
        when(mockTableRS.getString(3)).thenReturn(table);
        when(mockTableRS.next()).thenReturn(true).thenReturn(false); // just one element
        when(mockPKRS.getString(4)).thenReturn("pkey");
        when(mockPKRS.next()).thenReturn(true).thenReturn(false); // just one element
        when(mockColumnRS.next()).thenReturn(true).thenReturn(false);
        when(mockColumnRS.getString("COLUMN_NAME")).thenReturn("cName");
        when(mockColumnRS.getString(6)).thenReturn("cType");
                
        MetaData metaData = new TestMSQLMetaData(testProperties);
        List<MatchMetaData> result = metaData.getMetaData();
        assertEquals(1, result.size());
        assertEquals(table, result.get(0).getTableName());
        assertEquals(Arrays.asList("pkey"), result.get(0).getPkeys());
        assertEquals("cName", result.get(0).getColumnName());
        assertEquals("cType", result.get(0).getColumnType());
        
        verify(mockTableRS, times(1)).close();
        verify(mockColumnRS, times(1)).close();
    }
}
