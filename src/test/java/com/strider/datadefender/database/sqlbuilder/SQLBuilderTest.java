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



package com.strider.datadefender.database.sqlbuilder;

import java.util.Properties;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * One class to test all sqlbuilder.  Split out if this class becomes too large.
 * @author Akira Matsuo
 *
 */
public class SQLBuilderTest {
    private final String     schema     = "test_schema";
    private final String     table      = "test_table";
    private final Properties noSchema   = new Properties();
    @SuppressWarnings("serial")
    private final Properties withSchema = new Properties() {
        {
            setProperty("schema", schema);
        }
    };

    @Test
    public void testLimit() {
        ISqlBuilder builder = new MsSqlBuilder(noSchema);

        assertEquals("SELECT TOP 1 ", builder.buildSelectWithLimit("SELECT ", 1));
        assertEquals("", builder.buildSelectWithLimit("", 0));
        builder = new MySQLSQLBuilder(noSchema);
        assertEquals(" LIMIT 1", builder.buildSelectWithLimit("", 1));
        assertEquals("", builder.buildSelectWithLimit("", 0));
        builder = new OracleSqlBuilder(noSchema);
        assertEquals(" AND rownum <= 1", builder.buildSelectWithLimit("", 1));
        assertEquals("", builder.buildSelectWithLimit("", 0));
    }

    @Test
    public void testSchemaPrefix() {
        ISqlBuilder builder = new MsSqlBuilder(noSchema);

        assertEquals("[" + table + "]", builder.prefixSchema(table));
        builder = new MsSqlBuilder(withSchema);
        assertEquals("[" + schema + "].[" + table + "]", builder.prefixSchema(table));
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
