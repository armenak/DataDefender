package com.strider.datadefender.requirement.file;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zaahid Bateson
 */
public class VersionUtilTest {

    @Test
    public void testIsCompatible() {
        boolean result = VersionUtil.isCompatible("1.0", "1.0");
        assertEquals(true, result);
    }

    @Test
    public void testIsCompatibleDoublePrecision() {
        boolean result = VersionUtil.isCompatible("1.2", "1.20");
        assertEquals(true, result);
    }

    @Test
    public void testIsCompatibleMinorFileLess() {
        boolean result = VersionUtil.isCompatible("1.2", "1.0");
        assertEquals(true, result);
    }

    @Test
    public void testIsCompatibleMinorFileLessDoublePrecision() {
        boolean result = VersionUtil.isCompatible("1.2", "1.02");
        assertEquals(true, result);
    }

    @Test
    public void testIsCompatibleMinorFileMore() {
        boolean result = VersionUtil.isCompatible("1.2", "1.3");
        assertEquals(false, result);
    }

    @Test
    public void testIsCompatibleMinorFileMoreDoublePrecision() {
        boolean result = VersionUtil.isCompatible("1.02", "1.2");
        assertEquals(false, result);
    }

    @Test
    public void testIsCompatibleMajorFileLess() {
        boolean result = VersionUtil.isCompatible("2.0", "1.0");
        assertEquals(false, result);
    }

    @Test
    public void testIsCompatibleMajorFileMore() {
        boolean result = VersionUtil.isCompatible("1.0", "2.0");
        assertEquals(false, result);
    }

    @Test
    public void testIsCompatibleMajorFileDoublePrecision() {
        boolean result = VersionUtil.isCompatible("01.1", "1.1");
        assertEquals(true, result);
    }

    @Test
    public void testThrowsExceptionInvalidAppVersion() {
        assertThrows(IllegalArgumentException.class, () -> {
            VersionUtil.isCompatible("test", "1.0");
        });
    }

    @Test
    public void testThrowsExceptionInvalidFileVersion() {
        assertThrows(IllegalArgumentException.class, () -> {
            VersionUtil.isCompatible("1.0", "test");
        });
    }
}
