package com.strider.dataanonymizer.functions;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Armenak Grigoryan
 */
public class UtilsTest extends TestCase {
    
    /**
     * Initializes logger
     */
    private static Logger log = getLogger(UtilsTest.class);    
    
    public UtilsTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getClassName method, of class Utils.
     */
    public void testGetClassName() {
        log.info("Executing testGetClassName ...");
        
        String fullClassName = "com.strider.dataanonymizer.functions.CoreFunctions";
        String expResult = "com.strider.dataanonymizer.functions";
        
        log.debug("Parameter: " + fullClassName);
        log.debug("Expected result:" + expResult);

        String result = Utils.getClassName(fullClassName);
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getMethodName method, of class Utils.
     */
    public void testGetMethodName() {
        String fullClassName = "com.strider.dataanonymizer.functions.CoreFunctions";
        String expResult = "CoreFunctions";
        
        log.debug("Parameter: " + fullClassName);
        log.debug("Expected result:" + expResult);

        String result = Utils.getMethodName(fullClassName);
        
        assertEquals(expResult, result);
    }    
}
