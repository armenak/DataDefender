package com.strider.dataanonymizer.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


/**
 *
 * @author Armenak Grigoryan
 */
public class DBConnectionFactory {
    
    private static final Logger log = getLogger(DBConnectionFactory.class);
    
    public static IDBConnection createDBConnection(final Properties databaseProperties) 
    throws DatabaseAnonymizerException {
        
        String database = databaseProperties.getProperty("database");
        if (database.equalsIgnoreCase("mysql")){
            return new MySQLDBConnection();
        } 
        
        throw new IllegalArgumentException("Database " + database + " is not supported");
    }
}
