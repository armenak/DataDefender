/*
 *
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

import java.sql.Connection;

import java.util.Properties;

import static java.sql.DriverManager.getConnection;

import com.strider.datadefender.DatabaseDiscoveryException;

/**
 *
 * @author sdi
 */
public class MSSQLDBConnection extends DBConnection {
    public MSSQLDBConnection(final Properties properties) throws DatabaseDiscoveryException {
        super(properties);
    }

    /**
     * Establishes database connection
     * @return Connection
     * @throws DatabaseAnonymizerException
     */
    @Override
    public Connection connect() throws DatabaseDiscoveryException {
        return doConnect(() -> getConnection(this.getURL()));
    }

    /**
     * Get connection url.  Package-level for testing purposes.
     * @return String
     */
    protected String getURL() {
        final StringBuilder sqlServerURL = new StringBuilder(this.url);

        sqlServerURL.append(";user=").append(this.userName).append(";password=").append(this.password);

        return sqlServerURL.toString();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
