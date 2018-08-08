package com.strider.datadefender.specialcase;

import com.strider.datadefender.Probability;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.strider.datadefender.database.metadata.MatchMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author strider
 */
public class SinDetectorTest {

    private static List <String> phiList;
    private static List<String> phiKeys;
    private static final String sin_with_argument = "583336318";
    private static final String sin_without_argument = "";    
    private static MatchMetaData metaData;
    private static MatchMetaData expResult;
    
    @Before
    public void setUp() {
        phiList = new ArrayList();
        phiList.add("test");
        phiList.add("depression");         
        
        phiKeys = new ArrayList();
        phiKeys.add("id");
        
        metaData = new MatchMetaData("test_schema","test_table",phiKeys,"test_column","VARCHAR",10);
        
        expResult = new MatchMetaData("test_schema","test_table",phiKeys,"test_column","VARCHAR",10);        
    }
    
    /**
     * Test of detectSin method, of class SinDetector.
     */
    @Test
    public void testDetectSinWithArgument() {
        expResult.setModel("sin");
        expResult.setAverageProbability(100);
        final List<Probability> probabilityList = new ArrayList();
        probabilityList.add(new Probability(sin_with_argument, 1.00));
        expResult.setProbabilityList(probabilityList);        
        
        final MatchMetaData result = SinDetector.detectSin(metaData, sin_with_argument);
        
        assertEquals(result.getModel(),expResult.getModel());        
    }
    
    @Test
    public void testDetectSinWithoutArgument() {
        final MatchMetaData result = PHIDetector.isPHITerm(metaData, sin_without_argument);
        assertNull(result);
    }    
}
