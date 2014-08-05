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
    
    public static IDBConnection createDBConnection(final String databasePropertyFile) 
    throws DatabaseAnonymizerException {
        
        if ((databasePropertyFile == null) || databasePropertyFile.isEmpty()) {
            log.error("Database properties are not defined");
            throw new IllegalArgumentException("Database properties are not defined");
        }
        
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(databasePropertyFile);
            props.load(is);
            is.close();
        } catch (IOException ex) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    log.error(ioe.toString());
                }
            }
            log.error(ex.toString());
        }
        
        String database = props.getProperty("database");
        if (database.equalsIgnoreCase("mysql")){
            return new MySQLDBConnection();
        } 
        
        throw new IllegalArgumentException("Database " + database + " is not supported");
    }
}
