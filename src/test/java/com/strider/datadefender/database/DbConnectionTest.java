package com.strider.datadefender.database;

import com.strider.datadefender.DbConfig;

import java.sql.Connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Using mock to test Connection.
 * @author Akira Matsuo
 */
@ExtendWith(MockitoExtension.class)
public class DbConnectionTest {

    @Mock
    DbConfig config;
    @Mock
    Connection mockConnection;

    @Test
    public void testConnect() throws Exception {
        final DbConnection test = new DbConnection(config) {
            @Override
            public Connection connect() throws DatabaseException {
                return doConnect(() -> mockConnection);
            }
        };
        assertEquals(mockConnection, test.connect());
        verify(mockConnection).setAutoCommit(false);
    }

    @Test
    public void testConnectSupplier() throws Exception {
        when(config.getUrl()).thenReturn("jdbc:h2:mem:utest;MODE=MySQL;DB_CLOSE_DELAY=-1");
        when(config.getUsername()).thenReturn("username");
        when(config.getUsername()).thenReturn("password");

        final DbConnection test = new DbConnection(config);
        final Connection con = test.connect();
        assertNotNull(con);
        assertFalse(con.isClosed());
        assertFalse(con.getAutoCommit());

        verify(config, atLeast(1)).getUrl();
        verify(config, atLeast(1)).getUsername();
        verify(config, atLeast(1)).getPassword();
    }
}
