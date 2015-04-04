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

package com.strider.dataanonymizer;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.database.IDBFactory;

import java.util.Properties;

/**
 * Defines contract for all generators
 * @author Matthew Eaton
 */
public interface IGenerator {
    /**
     * Generate data
     * @param databaseProperties Database property file name and path
     * @param anonymizerProperties Anonymizer property file name and path
     * @throws DatabaseAnonymizerException
     */
    void generate(IDBFactory dbFactory, Properties anonymizerProperties) throws DatabaseAnonymizerException;

}
