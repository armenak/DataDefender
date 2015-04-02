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
package com.strider.dataanonymizer.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.strider.dataanonymizer.database.metadata.ColumnMetaData;

/**
 * Simple tests for h2 db in mysql-mode.
 * Tests functionality of IDBFactory (h2/mysql) against the in memory DB.
 * 
 * @author Akira Matsuo
 */
public class H2DBTest {
    
    @SuppressWarnings("serial")
    private static Properties h2Props = new Properties() {{
        setProperty("vendor", "h2");
        setProperty("driver", "org.h2.Driver");
        setProperty("url", "jdbc:h2:mem:utest;MODE=MySQL;DB_CLOSE_DELAY=-1");
        setProperty("username", "test");
        setProperty("password", "");
    }};

    private static void setUpDB() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            //stmt.executeUpdate( "DROP TABLE table1" );
            stmt.executeUpdate("CREATE TABLE ju_users ( " +
                "id MEDIUMINT NOT NULL AUTO_INCREMENT, fname VARCHAR(50), lname VARCHAR(50), PRIMARY KEY (id) )" );
            stmt.executeUpdate("INSERT INTO ju_users ( fname, lname ) VALUES ( 'Claudio', 'Bravo' )");
            stmt.executeUpdate("INSERT INTO ju_users ( fname, lname ) VALUES ( 'Ugo', 'Bernasconi' )");
        }
    }
    
    private static IDBFactory factory = IDBFactory.get(h2Props);
    private static Connection con;
    @BeforeClass
    public static void classSetUp() throws DatabaseAnonymizerException, SQLException {
        con = factory.createDBConnection().connect();
        setUpDB();
    }
    @AfterClass
    public static void classTearDown() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("DROP TABLE ju_users");
        } finally {
            con.close();
        }
    }

    private void assertQuery(String[] asserts) throws SQLException {
        try (Statement stmt = con.createStatement(); 
            ResultSet rs = stmt.executeQuery("SELECT * FROM ju_users");) {
            int i = 0;
            while(rs.next())  {
                String line = rs.getInt("id") + ": " + rs.getString("fname") + ", " + rs.getString("lname");
                assertEquals(asserts[i], line);
                i++;
            }
            assertEquals(asserts.length, i);
        }
    }
    
    @Test
    public void testData() throws DatabaseAnonymizerException, SQLException {
        assertQuery(new String[] { "1: Claudio, Bravo", "2: Ugo, Bernasconi"});
    }
    
    @Test
    public void testMetaData() throws DatabaseAnonymizerException {
        List<ColumnMetaData> meta = factory.fetchMetaData().getMetaData();
        List<String> actual = meta.stream().filter(d -> d.getTableName().equals("ju_users"))
            .map(d -> d.toVerboseStr())
            .collect(Collectors.toList());
        List<String> expected = Arrays.asList("ju_users.id(integer)", "ju_users.fname(varchar)", "ju_users.lname(varchar)");
        assertTrue(expected.equals(actual));
    }
    
    @Test
    public void testSQLBuilder() {
        String sql = factory.createSQLBuilder().buildSelectWithLimit("SELECT * FROM blah", 1);
        assertEquals("SELECT * FROM blah LIMIT 1", sql);
    }
}
