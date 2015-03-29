package com.strider.dataanonymizer.database;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

/**
 * @author Akira Matsuo
 */
public class DBConnectionFactoryTest {

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidNoProps() throws DatabaseAnonymizerException {
        Properties invalidProps = new Properties();
        DBConnectionFactory.createDBConnection(invalidProps );
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidProps() throws DatabaseAnonymizerException {
        Properties invalidProps = new Properties();
        invalidProps.setProperty("vendor", "my-invalid-db");
        DBConnectionFactory.createDBConnection(invalidProps );
    }
    
    @Test
    public void testValidProps() throws DatabaseAnonymizerException {
        Properties validProps = new Properties();
        // mysql should be provided via mvn dependencies
        validProps.setProperty("vendor", "mysql");
        validProps.setProperty("driver", "com.mysql.jdbc.Driver");
        IDBConnection con = DBConnectionFactory.createDBConnection(validProps );
        assertNotNull(con);
    }
}
