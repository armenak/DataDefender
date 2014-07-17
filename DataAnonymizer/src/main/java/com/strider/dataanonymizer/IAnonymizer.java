package com.strider.dataanonymizer;

/**
 * Defines contract for all anonymizers
 * @author strider
 */
public interface IAnonymizer {
    /**
     * Anonymizes data.
     */
    public void anonymize(String propertyFile);    
}
