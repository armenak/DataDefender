package com.strider.dataanonymizer.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author armenak
 */
public class TestDBConnection {

    public void connect(String url){
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            Connection connection = DriverManager.getConnection(url);
            System.out.println("Connected");
            Statement statement = connection.createStatement();
            String query = "select * from test";
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()){
                System.out.println(rs.getString(1));
            }            
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
            System.out.println(e.toString());
        }

    }


    public static void main(String[] args) {
        TestDBConnection connServer = new TestDBConnection();
        String url = "jdbc:sqlserver://localhost;user=sa;password=taverna68;databaseName=da_test";
        connServer.connect(url);

    }
}
