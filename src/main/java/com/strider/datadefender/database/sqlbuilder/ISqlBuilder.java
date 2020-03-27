/*
 * Copyright 2015, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.datadefender.database.sqlbuilder;

/**
 * Interface for all classes implementing SQL builder.
 *
 * @author Armenak Grigoryan
 */
public interface ISqlBuilder {

    /**
     * Add a limit to returned rows for the passed sqlString.
     *
     * @param sqlString
     * @param limit
     * @return
     */
    String buildSelectWithLimit(String sqlString, int limit);

    /**
     * Prefix table name with schema if present.
     *
     * @param tableName
     * @return
     */
    String prefixSchema(String tableName);
}


//~ Formatted by Jindent --- http://www.jindent.com
