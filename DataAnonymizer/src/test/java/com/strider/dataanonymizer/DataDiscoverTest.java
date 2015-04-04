package com.strider.dataanonymizer;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.strider.dataanonymizer.database.H2DB;

public class DataDiscoverTest extends H2DB {
    
    @SuppressWarnings("serial")
    final Properties sampleDProps = new Properties() {{ 
        setProperty("probability_threshold", "0.5" ); 
        setProperty("english_tokens", "target/classes/en-token.bin");
        setProperty("english_ner_person", "target/classes/en-ner-person.bin");
        setProperty("limit", "10");
    }};

    // Doesn't really test much, yet... but could if the data was setup correctly
    @Test
    public void testHappyPath() throws AnonymizerException { 
        IDiscoverer discoverer = new DataDiscoverer();
        List<String> output = discoverer.discover(factory, sampleDProps, new HashSet<String>());
        assertTrue(output.isEmpty());
    }
}
