package com.strider.dataanonymizer.database;

import java.util.Properties;
import java.sql.Connection;

import org.apache.log4j.Logger;

/**
 *
 * @author Armenak Grigoryan
 */
public class DBConnectionFactory {
    
    private static final Logger log = Logger.getLogger(DBConnectionFactory.class);
    
    public static Connection createDBConnection(final Properties props) 
    throws DatabaseAnonymizerException {
        
        if (props == null) {
            log.error("Database properties are not defined");
            throw new IllegalArgumentException("Database properties are not defined");
        }
        
        String database = props.getProperty("database");
        if (database.equalsIgnoreCase("mysql")){
            return new MySQLDBConnection().connect(props);
        } 
        
        throw new IllegalArgumentException("Database " + database + " is not supported");
    }
}
