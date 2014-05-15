package com.strider.dataanonymizer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import com.strider.dataanonymizer.utils.AppProperties;
import java.util.Iterator;

/**
 *
 * @author strider
 */
public class ColumnDiscoverer implements IDiscoverer { 
    @Override
    public void discover(Connection conn) {
        
        System.out.println("Field discoverer");
        // Get the metadata from the the database
        SortedMap map = new TreeMap();
        try {
            // Getting all tables name
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String tableName = rs.getString(3);
                ResultSet resultSet = md.getColumns(null, null, tableName, null);        
                while (resultSet.next()) {
                    String columnName = resultSet.getString("COLUMN_NAME");
                    map.put(tableName, columnName);
                    System.out.println("table:"+tableName+" column:"+columnName);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        
        
        // Get the list of "suspicios" field names from property file
        InputStream input = null;
        try{
            input = AppProperties.class.getClassLoader().getResourceAsStream("fields.properties");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(input);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
                // Print the content on the console
                System.out.println (strLine);
            }
            //Close the input stream
            in.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
       
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            System.out.println("key : " + key + " value :" + map.get(key));
        }
        
        // Find out if database columns contain any of of the "suspicios" fields

        
        
        // Report column names
        
    }
}