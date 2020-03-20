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
 */
package com.strider.datadefender.database;

import java.sql.Connection;
import static java.sql.DriverManager.getConnection;

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.DbConfig;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author sdi
 */
public class MsSqlDbConnection extends DbConnection {

    public MsSqlDbConnection(DbConfig config) throws DataDefenderException {
        super(config);
    }
    
    /**
     * Establishes database connection
     *
     * @return Connection
     * @throws DatabaseException
     */
    @Override
    public Connection connect() throws DatabaseException {
        return doConnect(() -> getConnection(this.getUrl()));
    }

    /**
     * Get connection URL.
     * @return String
     */
    protected String getUrl() {
        StringBuilder sqlServerUrl = new StringBuilder(config.getUrl());
        if (!StringUtils.isBlank(config.getUsername())) {
            sqlServerUrl.append(";user=").append(config.getUsername());
        }
        if (!StringUtils.isBlank(config.getPassword())) {
            sqlServerUrl.append(";password=").append(config.getPassword());
        }
        return sqlServerUrl.toString();
    }
}
