package com.strider.datadefender.specialcase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.strider.datadefender.Probability;
import com.strider.datadefender.database.metadata.MatchMetaData;

/**
 *
 * @author strider
 */
public class SinDetectorTest {
    private static final String  sin_with_argument    = "583336318";
    private static final String  sin_without_argument = "";
    private static List<String>  phiList;
    private static List<String>  phiPKeys;
    private static List<String>  phiFKeys;    
    private static MatchMetaData metaData;
    private static MatchMetaData expResult;

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

        assertEquals(result.getModel(), expResult.getModel());
    }

    @Test
    public void testDetectSinWithoutArgument() {
        final MatchMetaData result = PHIDetector.isPHITerm(metaData, sin_without_argument);

        assertNull(result);
    }

    @Before
    public void setUp() {
        phiList = new ArrayList();
        phiList.add("test");
        phiList.add("depression");
        phiPKeys = new ArrayList();
        phiPKeys.add("id");
        phiFKeys = new ArrayList();
        phiFKeys.add("id2");        
        metaData  = new MatchMetaData("test_schema", "test_table", phiPKeys, phiFKeys, "test_column", "VARCHAR", 10);
        expResult = new MatchMetaData("test_schema", "test_table", phiPKeys, phiFKeys, "test_column", "VARCHAR", 10);
    }
}