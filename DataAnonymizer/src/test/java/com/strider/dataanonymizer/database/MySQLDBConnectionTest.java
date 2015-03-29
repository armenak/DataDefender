package com.strider.dataanonymizer.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.strider.dataanonymizer.utils.ISupplierWithException;

/**
 * Using mock to test Connection.
 * @author Akira Matsuo
 */
@RunWith(MockitoJUnitRunner.class)  
public class MySQLDBConnectionTest {
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
    private class TestMySQLDBConnection extends MySQLDBConnection {
        public TestMySQLDBConnection(Properties properties) throws DatabaseAnonymizerException {
            super(properties);
        }
        @Override
        protected Connection doConnect(ISupplierWithException<Connection, SQLException> supplier) throws DatabaseAnonymizerException {
            Field[] allFields = supplier.getClass().getDeclaredFields();
            assertEquals(1, allFields.length);
            Field field = allFields[0];
            field.setAccessible(true);
            try { // not exactly a great test, but checks that supplier has parent's properties at least
                String representation = ReflectionToStringBuilder.toString(field.get(supplier));
                assertTrue(representation.contains(
                    "[driver=java.util.List,vendor=mysql,url=invalid-url,userName=invalid-user,password=invalid-pass]"));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return mockConnection;
        }
    }
    
    @Test
    public void testConnect() throws DatabaseAnonymizerException, SQLException {
        TestMySQLDBConnection testDB = new TestMySQLDBConnection(testProps);
        assertEquals(mockConnection, testDB.connect());
    }
}
