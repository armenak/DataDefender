package com.strider.dataanonymizer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author strider
 */
public class ColumnDiscoverer implements IDiscoverer { 
    @Override
    public void discover(Connection conn) {
        
        System.out.println("Field discoverer");
        // Get the metadata from the the database
        try {
            // Getting all tables name
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            System.out.println("List of tables in database");
            while (rs.next()) {
                String tableName = rs.getString(3);
                System.out.println(tableName);
                ResultSet resultSet = md.getColumns(null, null, tableName, null);        
                while (resultSet.next()) {
                    String name = resultSet.getString("COLUMN_NAME");
                    String type = resultSet.getString("TYPE_NAME");
                    int size = resultSet.getInt("COLUMN_SIZE");
                    System.out.println("Column name: [" + name + "]; type: [" + type + "]; size: [" + size + "]");
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        
        
        // Get the list of "suspicios" field names from property file
        
        // Find out if database columns contain any of of the "suspicios" fields
        
        // Report column names
        
    }
}
