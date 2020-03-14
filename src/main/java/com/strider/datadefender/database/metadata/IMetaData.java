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
package com.strider.datadefender.database.metadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Public interface to define the contract for all database-specific
 * metadata classes.
 *
 * @author armenak
 */
public interface IMetaData {
    List<TableMetaData> getMetaData() throws SQLException;
    TableMetaData getMetaDataFor(final ResultSet rs) throws SQLException;
}
