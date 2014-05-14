package com.strider.dataanonymizer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 
 * @author Armenak Grigoryan
 */
public class MySQLDBConnection implements IDBConnection {
    
    @Override
    public Connection connect(final Properties props) {
        
        try {
            Class.forName(props.getProperty("driver"));
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                props.getProperty("url"), 
                props.getProperty("username"), 
                props.getProperty("password"));
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        
        return conn;
    }
}