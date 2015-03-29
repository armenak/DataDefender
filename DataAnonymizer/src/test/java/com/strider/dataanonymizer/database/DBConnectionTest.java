package com.strider.dataanonymizer.database;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Using mock to test Connection.
 * @author Akira Matsuo
 */
@RunWith(MockitoJUnitRunner.class)  
public class DBConnectionTest {
    @SuppressWarnings("serial")
    private Properties testProps = new Properties() {{
        setProperty("vendor", "mysql");
        setProperty("driver", "java.util.List");
        setProperty("url", "invalid-url");
        setProperty("username", "invalid-user");
        setProperty("password", "invalid-pass");
    }};
    @Mock  
    private Connection mockConnection; 
    // testing class
    private class TestDBConnection extends DBConnection {
        public TestDBConnection(Properties properties) throws DatabaseAnonymizerException {
            super(properties);
        }
        @Override
        public Connection connect() throws DatabaseAnonymizerException {
            return doConnect(() -> {
                this.runAsserts();
                return mockConnection;
            });
        }
        public void runAsserts() {
            assertEquals("mysql", this.vendor);
            assertEquals("java.util.List", this.driver);
            assertEquals("invalid-url", this.url);
            assertEquals("invalid-user", this.userName);
            assertEquals("invalid-pass", this.password);
        }
    }

    @Test
    public void testCtor() throws DatabaseAnonymizerException {
        TestDBConnection testDB = new TestDBConnection(testProps);
        testDB.runAsserts();
    }
    
    @Test
    public void testConnect() throws DatabaseAnonymizerException, SQLException {
        TestDBConnection testDB = new TestDBConnection(testProps);
        assertEquals(mockConnection, testDB.connect());
        // assert
        verify(mockConnection).setAutoCommit(false);
    }
}
