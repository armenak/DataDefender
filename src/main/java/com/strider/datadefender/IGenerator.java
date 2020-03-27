/*
 *
 * Copyright 2014, Armenak Grigoryan, Matthew Eaton, and individual contributors as indicated
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
package com.strider.datadefender;

import com.strider.datadefender.database.DatabaseException;
import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.requirement.Requirement;

/**
 * Defines contract for all generators
 * @author Matthew Eaton
 */
public interface IGenerator {

    /**
     * Generate data
     * @param databaseProperties Database property file name and path
     * @param Requirement requirement file
     * @throws DatabaseException
     */
    void generate(IDbFactory dbFactory, Requirement requirement) throws DatabaseException;
}
