package com.strider.dataanonymizer.utils;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 *
 * @author strider
 */
public class XegerTest extends TestCase {
    private static Logger log = getLogger(XegerTest.class);
    
    public XegerTest(String testName) {
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
     * Test of generate method, of class Xeger.
     */
    public void testGenerate() {
        log.debug("Generate string");
        String regex = "[ab]{4,6}c";
        Xeger instance = new Xeger(regex);
        for (int i = 0; i < 100; i++) {
            String text = instance.generate();
            assertTrue(text.matches(regex));
        }
    }
    
}
