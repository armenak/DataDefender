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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.strider.dataanonymizer.utils.IConsumerWithException;

/**
 * Handles the setup and teardown of h2db for testing.
 * 
 * @author Akira Matsuo
 */
public abstract class H2DB {

    @SuppressWarnings("serial")
    private static Properties h2Props = new Properties() {{
        setProperty("vendor", "h2");
        setProperty("driver", "org.h2.Driver");
        setProperty("url", "jdbc:h2:mem:utest;MODE=MySQL;DB_CLOSE_DELAY=-1");
        setProperty("username", "test");
        setProperty("password", "");
    }};

    protected static IDBFactory factory = IDBFactory.get(h2Props);
    protected static Connection con;

    @BeforeClass
    public static void setUpDB() throws DatabaseAnonymizerException, SQLException {
        con = factory.getConnection().connect();
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("CREATE TABLE ju_users ( " +
                "id MEDIUMINT NOT NULL AUTO_INCREMENT, fname VARCHAR(50), lname VARCHAR(50), PRIMARY KEY (id) )" );
            stmt.executeUpdate("INSERT INTO ju_users ( fname, lname ) VALUES ( 'Claudio', 'Bravo' )");
            stmt.executeUpdate("INSERT INTO ju_users ( fname, lname ) VALUES ( 'Ugo', 'Bernasconi' )");
        } // make sure other connections can see this data, too
        con.commit();
    }
    @AfterClass
    public static void tearDownDB() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("DROP TABLE ju_users");
        } finally {
            con.close();
        }
    }
    
    // Helper methods to assist in testing data
    protected void consumeQuery(IConsumerWithException<ResultSet, SQLException> cons) throws SQLException {
        try (Statement stmt = con.createStatement(); 
            ResultSet rs = stmt.executeQuery("SELECT * FROM ju_users");) {
            cons.accept(rs);
        }
    }
    
    protected void assertInitialData(ResultSet rs) throws SQLException {
        assertData(rs, (expected, actual) -> assertEquals(expected, actual));
    }
    
    protected void assertData(ResultSet rs, BiConsumer<String, String> cons) throws SQLException {
        String[] asserts = new String[]{ "1: Claudio, Bravo", "2: Ugo, Bernasconi"};
        int i = 0;
        while(rs.next())  {
            String line = rs.getInt("id") + ": " + rs.getString("fname") + ", " + rs.getString("lname");
            cons.accept(asserts[i], line);
            i++;
        }
        assertEquals(asserts.length, i);
    }
}
