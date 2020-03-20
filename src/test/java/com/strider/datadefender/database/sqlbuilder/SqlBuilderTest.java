/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.datadefender.database.sqlbuilder;

import com.strider.datadefender.DbConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Zaahid Bateson <zaahid.bateson@ubc.ca>
 */
public class SqlBuilderTest {

    @Mock
    DbConfig mockConfig;

    @Test
    public void testBuildSelectWithLimit() {
        SqlBuilder test = new SqlBuilder(mockConfig);
        String sql = test.buildSelectWithLimit("toost", 1);
        assertEquals("toost LIMIT 1", sql);
    }

    @Test
    public void testBuildSelectWithZeroLimit() {
        SqlBuilder test = new SqlBuilder(mockConfig);
        String sql = test.buildSelectWithLimit("toost", 0);
        assertEquals("toost", sql);
    }

    @Test
    public void testPrefixSchema() {
        when(mockConfig.getSchema()).thenReturn("scheems");
        SqlBuilder test = new SqlBuilder(mockConfig);
        String tableName = test.prefixSchema("tablas");
        assertEquals("scheems.tablas", tableName);
    }

    @Test
    public void testPrefixBlankSchema() {
        when(mockConfig.getSchema()).thenReturn(null).thenReturn("").thenReturn(" ");
        SqlBuilder test = new SqlBuilder(mockConfig);
        assertEquals("tablas", test.prefixSchema("tablas"));
        assertEquals("tablas", test.prefixSchema("tablas"));
        assertEquals("tablas", test.prefixSchema("tablas"));
    }
}
