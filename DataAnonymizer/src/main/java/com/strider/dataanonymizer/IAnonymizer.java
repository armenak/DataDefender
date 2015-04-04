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

package com.strider.dataanonymizer;

import java.util.Collection;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import com.strider.dataanonymizer.database.IDBFactory;

import java.util.Properties;

/**
 * Defines contract for all anonymizers
 * @author strider
 */
public interface IAnonymizer {
    /**
     * Anonymizes data.
     * @param dbFactory
     * @param anonymizerProperties
     * @param tables Optional list of tables to anonymize -
     *        if the collection is empty, all tables specified in requirements
     *        are anonymized.
     * @throws com.strider.dataanonymizer.database.DatabaseAnonymizerException
     */
    void anonymize(IDBFactory dbFactory, Properties anonymizerProperties, Collection<String> tables) throws DatabaseAnonymizerException;    
}
