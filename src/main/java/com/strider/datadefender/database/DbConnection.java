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

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.DbConfig;
import com.strider.datadefender.utils.ISupplierWithException;

import java.sql.Connection;
import java.sql.SQLException;
import static java.sql.DriverManager.getConnection;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Handles standard database connections with username/password provided.
 */
@Log4j2
public class DbConnection implements IDbConnection {

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

    /**
     * Default implementation calls DriverManager.getConnection with the
     * provided jdbc url, username and password.
     *
     * @return Connection
     * @throws DatabaseAnonymizerException
     */
    @Override
    public Connection connect() throws DataDefenderException {
        if (!StringUtils.isNoneBlank(config.getUsername(), config.getPassword())) {
            return doConnect(() -> getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
            ));
        }
        return doConnect(() -> getConnection(config.getUrl()));
    }
}
