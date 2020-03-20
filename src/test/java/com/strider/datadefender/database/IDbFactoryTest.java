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
