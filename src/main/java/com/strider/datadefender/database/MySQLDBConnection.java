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

import com.strider.datadefender.DataDefenderException;

/**
 * MySQL database connection
 * @author Armenak Grigoryan
 */
public class MySQLDBConnection extends DBConnection {
    public MySQLDBConnection(final Properties properties) throws DataDefenderException {
        super(properties);
    }

    /**
     * Establishes database connection
     * @return Connection
     * @throws DatabaseAnonymizerException
     */
    @Override
    public Connection connect() throws DataDefenderException {
        return doConnect(() -> getConnection(this.url, this.userName, this.password));
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
