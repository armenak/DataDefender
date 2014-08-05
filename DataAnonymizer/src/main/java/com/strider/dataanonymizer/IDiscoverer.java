package com.strider.dataanonymizer;

/**
 * Defines contract for all discoverers
 * @author Armenak Grigoryan
 */
public interface IDiscoverer {
    
    /**
     * Discovers data or data containers with data which can be be the subject 
     * for data anonymization.
     * @param databasePropertyFile
     * @throws com.strider.dataanonymizer.AnonymizerException
     */
    void discover(String databasePropertyFile) throws AnonymizerException;    
    
    /**
     * Discovers data or data containers with data which can be be the subject 
     * for data anonymization.
     * @param databasePropertyFile
     * @param columnPropertyFile
     * @throws com.strider.dataanonymizer.AnonymizerException
     */
    void discover(String databasePropertyFile, String columnPropertyFile) throws AnonymizerException;
}
