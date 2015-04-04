package com.strider.dataanonymizer;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.strider.dataanonymizer.database.H2DB;

public class ColumnDiscoverTest extends H2DB {
    
    @SuppressWarnings("serial")
    final Properties sampleCProps = new Properties() {{ setProperty("fname", "true" ); }};
    @SuppressWarnings("serial")
    final Properties badCProps = new Properties() {{ setProperty("la colonna non esiste", "true" ); }};

    @Test
    public void testWithColumns() throws AnonymizerException { 
        IDiscoverer discoverer = new ColumnDiscoverer();
        List<String> suspects = discoverer.discover(factory, sampleCProps, new HashSet<String>());
        assertTrue(Arrays.asList("ju_users.fname").equals(suspects));
    }

    @Test
    public void testWithTablesColumns() throws AnonymizerException { 
        IDiscoverer discoverer = new ColumnDiscoverer();
        List<String> suspects = discoverer.discover(factory, sampleCProps, 
            new HashSet<String>(Arrays.asList("ju_users")));
        assertTrue(Arrays.asList("ju_users.fname").equals(suspects));
    }

    @Test
    public void testWithBadTablesColumns() throws AnonymizerException { 
        IDiscoverer discoverer = new ColumnDiscoverer();
        List<String> suspects = discoverer.discover(factory, sampleCProps, 
            new HashSet<String>(Arrays.asList("il tavolo non esiste")));
        assertTrue(suspects.isEmpty());
    }

    @Test
    public void testWithTablesBadColumns() throws AnonymizerException { 
        IDiscoverer discoverer = new ColumnDiscoverer();
        List<String> suspects = discoverer.discover(factory, badCProps, 
            new HashSet<String>(Arrays.asList("ju_users")));
        assertTrue(suspects.isEmpty());
    }
}
