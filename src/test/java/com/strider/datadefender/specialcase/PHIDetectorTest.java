
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
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
public class PHIDetectorTest {
    private static final String  phi_with_argument    = "depression";
    private static final String  phi_without_argument = "";
    private static List<String>  phiList;
    private static List<String>  phiPKeys;
    private static List<String>  phiFKeys;    
    private static MatchMetaData metaData;
    private static MatchMetaData expResult;

    /**
     * Test of isPHITerm method, of class PHIDetector.
     */
    @Test
    public void testIsPHITermWithArgument() {
        expResult.setModel("phi");
        expResult.setAverageProbability(100);

        final List<Probability> probabilityList = new ArrayList();

        probabilityList.add(new Probability(phi_with_argument, 1.00));
        expResult.setProbabilityList(probabilityList);

        final MatchMetaData result = PHIDetector.isPHITerm(metaData, phi_with_argument);

        assertEquals(result.getModel(), expResult.getModel());
    }

    @Test
    public void testIsPHITermWithoutArgument() {
        final MatchMetaData result = PHIDetector.isPHITerm(metaData, phi_without_argument);

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