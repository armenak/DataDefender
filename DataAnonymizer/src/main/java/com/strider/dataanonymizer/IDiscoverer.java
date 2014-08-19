package com.strider.dataanonymizer;

import java.util.Properties;

/**
 * Defines contract for all discoverers
 * @author Armenak Grigoryan
 */
public interface IDiscoverer {
    
    /**
     * Discovers data or data containers with data which can be be the subject 
     * for data anonymization.
     * @param databaseProperties
     * @param properties
     * @throws com.strider.dataanonymizer.AnonymizerException
     */
    void discover(Properties databaseProperties, Properties properties) throws AnonymizerException;
}
