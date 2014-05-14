package com.strider.dataanonymizer.database;

import java.sql.Connection;
import java.util.Properties;

/**
 * Interface for all classes implementing database connection
 * @author Armenak Grigoryan
 */
public interface IDBConnection {
    public Connection connect(final Properties props);
}
