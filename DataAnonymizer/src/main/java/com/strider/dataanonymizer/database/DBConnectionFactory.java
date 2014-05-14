package com.strider.dataanonymizer.database;

import java.util.Properties;
import java.sql.Connection;

/**
 *
 * @author Armenak Grigoryan
 */
public class DBConnectionFactory {
    public static Connection createDBConnection(final Properties props) {
        
        if (props == null) {
            throw new IllegalArgumentException("Database properties are not defined");
        }
        
        String database = props.getProperty("database");
        if (database.equalsIgnoreCase("mysql")){
            return new MySQLDBConnection().connect(props);
        } 
//        else if (database.equalsIgnoreCase("mssqlserver")) {
//            throw new IllegalArgumentException(database + " is not supported");
//        } else if (database.equalsIgnoreCase("oracle")) {
//            throw new IllegalArgumentException(database + " is not supported");
//        }
        
        throw new IllegalArgumentException("Database " + database + " is not supported");
    }
}
