package com.strider.dataanonymizer;

import com.strider.dataanonymizer.database.DatabaseAnonymizerException;
import java.util.Properties;

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
    void anonymize(Properties databaseProperties, Properties anonymizerProperties) throws DatabaseAnonymizerException;    
}
