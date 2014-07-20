package com.strider.dataanonymizer.database;

import static java.lang.Class.forName;
import static java.sql.DriverManager.getConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * MySQL database connection
 * @author Armenak Grigoryan
 */
public class MySQLDBConnection implements IDBConnection {

    private static final Logger log = getLogger(MySQLDBConnection.class);
    
    /**
     * Establishes database connection
     * @param props
     * @return
     * @throws DatabaseAnonymizerException 
     */
    @Override
    public Connection connect(final Properties props) throws DatabaseAnonymizerException {
        
        try {
            forName(props.getProperty("driver"));
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.toString());
            throw new DatabaseAnonymizerException(cnfe.toString());
        }
        
        Connection conn = null;
        try {
            conn = getConnection(
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