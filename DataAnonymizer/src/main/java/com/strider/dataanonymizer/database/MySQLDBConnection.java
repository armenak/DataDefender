package com.strider.dataanonymizer.database;

import com.strider.dataanonymizer.ColumnDiscoverer;
import static java.lang.Class.forName;
import static java.sql.DriverManager.getConnection;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

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
     * @param propertyFile
     * @return Connection
     * @throws DatabaseAnonymizerException 
     */
    @Override
    public Connection connect(String propertyFile) throws DatabaseAnonymizerException {

        Configuration configuration = null;
        try {
            configuration = new PropertiesConfiguration(propertyFile);
        } catch (ConfigurationException ex) {
            log.error(ColumnDiscoverer.class);
        }
        
        String driver = configuration.getString("driver");
        String database = configuration.getString("database");
        String url = configuration.getString("url");
        String userName = configuration.getString("username");
        String password = configuration.getString("password");
        log.debug("Using driver " + driver);
        log.debug("Database type: " + database);
        log.debug("Database URL: " + url);
        log.debug("Logging in using username " + userName); 
        
        try {
            forName(driver);
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.toString());
            throw new DatabaseAnonymizerException(cnfe.toString());
        }
        
        Connection conn = null;
        try {
            conn = getConnection(url, userName, password);
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