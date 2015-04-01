package com.strider.dataanonymizer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Using mock to test Connection.
 * @author Akira Matsuo
 */
@RunWith(MockitoJUnitRunner.class)  
public class H2DBTest {

    @Test
    public void testCtor() throws DatabaseAnonymizerException {
        try  {
            Class.forName("org.h2.Driver"); // mysql mode, and keep db open till jvm dies
            Connection con = DriverManager.getConnection("jdbc:h2:mem:utest;MODE=MySQL;DB_CLOSE_DELAY=-1", "test", "" );
            Statement stmt = con.createStatement();
            //stmt.executeUpdate( "DROP TABLE table1" );
            stmt.executeUpdate( "CREATE TABLE users ( fname varchar(50), lname varchar(50) )" );
            stmt.executeUpdate( "INSERT INTO users ( fname, lname ) VALUES ( 'Claudio', 'Bravo' )" );
            stmt.executeUpdate( "INSERT INTO users ( fname, lname ) VALUES ( 'Ugo', 'Bernasconi' )" );
 
            query(stmt);
            query(stmt);
            
            stmt.close();
            con.close();
        } catch( Exception e )  {
            System.out.println( e.getMessage() );
        }
    }

    private void query(Statement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM users");
        while(rs.next())  {
            String name = rs.getString("fname");
            String lname = rs.getString("lname");
            System.out.println(name + ", " + lname);
        }
    }

}
