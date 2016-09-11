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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.strider.datadefender.database.metadata.MatchMetaData;

/**
 * Simple tests for h2 db in mysql-mode.
 * Tests functionality of IDBFactory (h2/mysql) against the in memory DB.
 * 
 * @author Akira Matsuo
 */
public class H2DBTest extends H2DB {
    
    @Test
    public void testData() throws DatabaseAnonymizerException, SQLException {
        consumeQuery(this::assertInitialData);
    }

    @Test
    public void testMetaData() throws DatabaseAnonymizerException {
        List<MatchMetaData> meta = factory.fetchMetaData().getMetaData();
        List<String> actual = meta.stream().filter(d -> d.getTableName().equals("ju_users"))
            .map(d -> d.toVerboseStr())
            .collect(Collectors.toList());
        List<String> expected = Arrays.asList("null.ju_users.id(integer)", "null.ju_users.fname(varchar)", "null.ju_users.lname(varchar)");
        assertTrue(expected.equals(actual));
    }

    @Test
    public void testSQLBuilder() {
        String sql = factory.createSQLBuilder().buildSelectWithLimit("SELECT * FROM blah", 1);
        assertEquals("SELECT * FROM blah LIMIT 1", sql);
    }
}
