package com.strider.dataanonymizer.database;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

/**
 * @author Akira Matsuo
 */
public class IDBFactoryTest {

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidNoProps() throws DatabaseAnonymizerException {
        Properties invalidProps = new Properties();
        IDBFactory.get(invalidProps).createDBConnection();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidProps() throws DatabaseAnonymizerException {
        Properties invalidProps = new Properties();
        invalidProps.setProperty("vendor", "my-invalid-db");
        IDBFactory.get(invalidProps).createDBConnection();
    }
    
    @Test
    public void testValidProps() throws DatabaseAnonymizerException {
        Properties validProps = new Properties();
        // mysql should be provided via mvn dependencies
        validProps.setProperty("vendor", "mysql");
        validProps.setProperty("driver", "com.mysql.jdbc.Driver");
        IDBConnection con = IDBFactory.get(validProps).createDBConnection();
        assertNotNull(con);
    }
}
