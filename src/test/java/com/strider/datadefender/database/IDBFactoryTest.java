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

import java.util.Properties;

import org.junit.Test;

import com.strider.datadefender.DataDefenderException;

/**
 * @author Akira Matsuo
 */
public class IDBFactoryTest {
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidNoProps() throws DatabaseAnonymizerException, DataDefenderException {
        final Properties invalidProps = new Properties();

        IDbFactory.get(invalidProps).getConnection();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidProps() throws DatabaseAnonymizerException, DataDefenderException {
        final Properties invalidProps = new Properties();

        invalidProps.setProperty("vendor", "my-invalid-db");
        IDbFactory.get(invalidProps).getConnection();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
