package com.strider.datadefender.database;

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.DbConfig;
import com.strider.datadefender.DbConfig.Vendor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Akira Matsuo
 */
@ExtendWith(MockitoExtension.class)
public class IDbFactoryTest {

    @Mock
    DbConfig config;

    @Test
    public void testInvalidNoProps() {
        when(config.getVendor()).thenReturn(Vendor.H2);
        assertThrows(DatabaseException.class, () -> IDbFactory.get(config).getConnection());
    }

    @Test
    public void testInvalidProps() throws DatabaseException, DataDefenderException {
        when(config.getVendor()).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () ->
            IDbFactory.get(config).getConnection()
        );
    }
}
