package com.strider.dataanonymizer.database;

import com.strider.dataanonymizer.ColumnDiscoverer;
import static java.lang.Class.forName;
import java.sql.Connection;
import static java.sql.DriverManager.getConnection;
import static java.sql.DriverManager.getConnection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
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
    public Connection connect(Properties properties) throws DatabaseAnonymizerException {

        String driver = properties.getProperty("driver");
        String database = properties.getProperty("database");
        String url = properties.getProperty("url");
        String userName = properties.getProperty("username");
        String password = properties.getProperty("password");
        log.debug("Using driver " + driver);
        log.debug("Database type: " + database);
        log.debug("Database URL: " + url);
        log.debug("Logging in using username " + userName); 
        
        try {
            forName(driver);
        } catch (ClassNotFoundException cnfe) {
            log.error(cnfe.toString());
            throw new DatabaseAnonymizerException(cnfe.toString(), cnfe);
        }
        
        Connection conn = null;
        try {
            conn = getConnection(url, userName, password);
        } catch (SQLException sqle) {
            log.error(sqle.toString());
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sql) {
                    log.error(sql.toString());
                }
            }
            throw new DatabaseAnonymizerException(sqle.toString(), sqle);
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
            throw new DatabaseAnonymizerException(ex.toString(), ex);
        }
    }
}