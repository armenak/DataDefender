package com.strider.datadefender.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author strider
 */
public class XegerTest {

    private static final String REGEXP_1 = "[ab]{4,6}c";
    private static final String REGEXP_2 = "[0-9]{3}-[0-9]{3}-[0-9]{3}";

    /**
     * Test of generate method, of class Xeger.
     */
    @Test
    public void testGenerate() {
        System.out.println("Generate string");

        final Xeger instance = new Xeger(REGEXP_1);

        for (int i = 0; i < 100; i++) {
            final String text = instance.generate();

            assertTrue(text.matches(REGEXP_1));
        }
    }

    /**
     * Test of generate method, of class Xeger.
     */
    @Test
    public void testGenerateSIN() {
        System.out.println("Generate SIN");

        final Xeger  instance = new Xeger(REGEXP_2);
        final String text     = instance.generate();

        System.out.println(text);
        assertTrue(text.matches(REGEXP_2));
    }
}
