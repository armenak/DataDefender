package com.strider.dataanonymizer;

import java.sql.Connection;

/**
 * Defines contract for all discoverers
 * @author Armenak Grigoryan
 */
public interface IDiscoverer {
    
    /**
     * Discovers data or data containers with data which can be be the subject 
     * for data anonymization.
     * @param databasePropertyFile
     */
    public void discover(String databasePropertyFile);    
    
    /**
     * Discovers data or data containers with data which can be be the subject 
     * for data anonymization.
     * @param databasePropertyFile
     * @param columnPropertyFile
     */
    public void discover(String databasePropertyFile, String columnPropertyFile);
}
