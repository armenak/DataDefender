package com.strider.dataanonymizer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.log4j.Logger;

/**
 * MySQL database connection
 * @author Armenak Grigoryan
 */
public class MySQLDBConnection implements IDBConnection {

    private static final Logger log = Logger.getLogger(MySQLDBConnection.class);
    
    /**
     * Establishes database connection
     * @param props
     * @return
     * @throws DatabaseAnonymizerException 
     */
    @Override
    public Connection connect(final Properties props) throws DatabaseAnonymizerException {
        
        try {
            Class.forName(props.getProperty("driver"));
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.toString());
            throw new DatabaseAnonymizerException(cnfe.toString());
        }
        
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                props.getProperty("url"), 
                props.getProperty("username"), 
                props.getProperty("password"));
        } catch (SQLException sqle) {
            log.error(sqle.toString());
            throw new DatabaseAnonymizerException(sqle.toString());
        }
        
        return conn;
    }
    
    /**
     * Closes database connection
     * @param conn
     * @throws DatabaseAnonymizerException 
     */
    @Override
    public void disconnect(final Connection conn) throws DatabaseAnonymizerException {
        try {
            conn.close();
        } catch (SQLException ex) {
            log.error(ex.toString());
            throw new DatabaseAnonymizerException(ex.toString());
        }
    }
}