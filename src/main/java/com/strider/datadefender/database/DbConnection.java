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
import java.sql.SQLException;

import java.util.Properties;

import static java.lang.Class.forName;

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.DbConfig;
import com.strider.datadefender.DbConfig.Vendor;
import com.strider.datadefender.utils.ISupplierWithException;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract class handling database connections
 */
@Slf4j
public abstract class DbConnection implements IDBConnection {

    protected final DbConfig config;

    /**
     * Default constructor, initializes config.
     * @param config
     */
    public DbConnection(final DbConfig config) throws DataDefenderException {
        this.config = config;
    }

    /**
     * Handles the actual creating of the connection, by running the supplier
     * method provided by subclasses.
     *
     * @param supplier
     * @return
     * @throws DatabaseAnonymizerException
     */
    protected Connection doConnect(
        final ISupplierWithException<Connection, SQLException> supplier
    ) throws DataDefenderException {
        Connection conn = null;
        try {
            log.info("Establishing database connection");
            conn = supplier.get();
            conn.setAutoCommit(false);
        } catch (SQLException sqle) {
            log.error(sqle.toString());
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sql) {
                    log.error(sql.toString());
                }
            }
            throw new DatabaseAnonymizerException(sqle.toString(), sqle);
        }
        return conn;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
