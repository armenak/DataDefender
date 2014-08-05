package com.strider.dataanonymizer.database;

import java.sql.Connection;

/**
 * Interface for all classes implementing database connection
 * @author Armenak Grigoryan
 */
public interface IDBConnection {
    Connection connect(final String propertyFile) throws DatabaseAnonymizerException;
    void disconnect(final Connection conn) throws DatabaseAnonymizerException;    
}
