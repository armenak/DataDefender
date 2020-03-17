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

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import com.strider.datadefender.DataDefenderException;
import com.strider.datadefender.DbConfig;
import com.strider.datadefender.DbConfig.Vendor;
import com.strider.datadefender.database.metadata.IMetaData;
import com.strider.datadefender.database.metadata.MetaData;
import com.strider.datadefender.database.metadata.MySqlMetaData;
import com.strider.datadefender.database.sqlbuilder.ISqlBuilder;
import com.strider.datadefender.database.sqlbuilder.SqlBuilder;
import com.strider.datadefender.database.sqlbuilder.MsSqlBuilder;
import com.strider.datadefender.database.sqlbuilder.OracleSqlBuilder;
import com.strider.datadefender.utils.ICloseableNoException;

import lombok.extern.log4j.Log4j2;

/**
 * Aggregate all the various db factories.
 *
 * Will handle the 'pooling' of connections (currently only one).
 *
 * All clients should close the connection by calling the close() method of the
 * AutoCloseable interface.
 *
 * @author Akira Matsuo
 */
public interface IDbFactory extends ICloseableNoException {

    ISqlBuilder createSQLBuilder() throws DataDefenderException;
    IMetaData fetchMetaData() throws DataDefenderException;
    Connection getConnection();
    Connection getUpdateConnection();
    String getVendorName();

    /**
     * Implements the common logic of getting/closing database connections
     */
    @Log4j2
    static abstract class DbFactory implements IDbFactory {

        private final Connection connection;
        protected Connection updateConnection;
        private final Vendor vendor;

        DbFactory(Vendor vendorName) throws DataDefenderException {
            log.info("Connecting to database");
            connection = createConnection();
            updateConnection = connection;
            vendor = vendorName;
        }

        @Override
        public void close() {
            if (connection == null) {
                return;
            }
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }

        public abstract Connection createConnection() throws DataDefenderException;

        @Override
        public Connection getConnection() {
            return connection;
        }

        @Override
        public Connection getUpdateConnection() {
            return updateConnection;
        }

        @Override
        public String getVendorName() {
            return vendor.name();
        }
    }

    /**
     * Generic method handling creation of a DBFactory anonymous instance for
     * supported database vendors.
     *
     * @param <D>
     * @param <M>
     * @param <S>
     * @param config
     * @param connection
     * @param metaData
     * @param builder
     * @return
     * @throws DataDefenderException
     */
    private static <D extends IDbConnection, M extends IMetaData, S extends ISqlBuilder> DbFactory getFactoryWith(
        final DbConfig config,
        Class<D> connection,
        Class<M> metaData,
        Class<S> builder
    ) throws DataDefenderException {
        return new DbFactory(config.getVendor()) {
            @Override
            public Connection createConnection() throws DataDefenderException {
                try {
                    return connection.getConstructor(DbConfig.class).newInstance(config).connect();
                } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
                    throw new DataDefenderException("", e);
                }
            }
            @Override
            public IMetaData fetchMetaData() throws DataDefenderException {
                try {
                    return metaData.getConstructor(DbConfig.class, Connection.class).newInstance(config, getConnection());
                } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
                    throw new DataDefenderException("", e);
                }
            }
            @Override
            public ISqlBuilder createSQLBuilder() throws DataDefenderException {
                try {
                    return builder.getConstructor(DbConfig.class).newInstance(config);
                } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e) {
                    throw new DataDefenderException("", e);
                }
            }
        };
    }

    /**
     * Creates and returns an IDBFactory instance for the given rdbms.
     *
     * @param config
     * @return db factory instance
     * @throws DatabaseAnonymizerException
     */
    public static IDbFactory get(final DbConfig config) throws DataDefenderException {

        if (config.getVendor() == Vendor.MYSQL || config.getVendor() == Vendor.H2) {
            DbFactory factory = getFactoryWith(config, DbConnection.class, MySqlMetaData.class, SqlBuilder.class);
            // create separate connection for updates
            factory.updateConnection = factory.createConnection();
            return factory;
        } else if (config.getVendor() == Vendor.SQLSERVER) {
            return getFactoryWith(config, MsSqlDbConnection.class, MetaData.class, MsSqlBuilder.class);
        } else if (config.getVendor() == Vendor.ORACLE) {
            return getFactoryWith(config, DbConnection.class, MetaData.class, OracleSqlBuilder.class);
        } else if (config.getVendor() == Vendor.POSTGRESQL) {
            return getFactoryWith(config, DbConnection.class, MetaData.class, SqlBuilder.class);
        }

        throw new IllegalArgumentException("Database " + config.getVendor() + " is not supported");
    }
}
