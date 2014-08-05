package com.strider.dataanonymizer;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;

/**
 * Defines contract for all anonymizers
 * @author strider
 */
public interface IAnonymizer {
    /**
     * Anonymizes data.
     * @param propertyFile
     * @param String propertyFile
     * @throws com.strider.dataanonymizer.database.DatabaseAnonymizerException
     */
    void anonymize(String propertyFile, String anonymizerPropertFile) throws DatabaseAnonymizerException;    
}
