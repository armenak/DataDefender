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
package com.strider.dataanonymizer.database.metadata;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author armenak
 * 
 * Essentially does the same thing as the MetaData class, but creating
 * an extra class here just to be explicit and to carry on the current model.
 */
public class OracleMetaData extends MSSQLMetaData {
    
    public OracleMetaData(final Properties databaseProperties, Connection connection) {
        super(databaseProperties, connection);
    }
}