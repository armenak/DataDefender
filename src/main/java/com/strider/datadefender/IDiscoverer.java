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

package com.strider.datadefender;

/**
 * Defines contract for all discoverers
 * @author Armenak Grigoryan
 */
public interface IDiscoverer {
    
    /**
     * Discovers data or data containers with data which can be be the subject 
     * for data anonymization.  Results found should be stored in matches member variable.
     * @param dbFactory
     * @param properties
     * @param tables Optional list of tables to anonymize -
     *        if the collection is empty, all tables specified in requirements
     *        are anonymized.
     * @throws com.strider.datadefender.AnonymizerException
     * @returns List of results introduced for testing purposes.
     */
    //List<MatchMetaData> discover(IDBFactory dbFactory, Properties dataDiscoveryProperties, Set<String> tables) throws AnonymizerException;

    //List<MatchMetaData> discover(Properties dataDiscoveryProperties) throws AnonymizerException;    
    
    /**
     * Creates a sample requirement file from a list of sorted matches found by the discovery method.  
     * The requirement file will most likely require customization (for example; anonymzation function) before you want to run the Anonymizer against it.
     * @param fileName
     * @throws AnonymizerException
     */
    void createRequirement(String fileName)throws AnonymizerException;
}
