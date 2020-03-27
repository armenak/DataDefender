package com.strider.datadefender.anonymizer.functions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zaahid Bateson
 */
public class CoreTest {

    @Test
    public void testRandomStringFromStream() throws Exception {

        List<String> read = List.of("toost", "bloost");
        InputStream is = CoreTest.class.getClassLoader().getResourceAsStream("core-test.txt");

        Core instance = new Core();
        String first = instance.randomStringFromStream("test", () -> is);
        assertNotNull(first);
        assertTrue(read.contains(first));

        String second = instance.randomStringFromStream("test", () -> is);
        assertNotNull(second);
        assertNotEquals(first, second);
        assertTrue(read.contains(second));

        String third = instance.randomStringFromStream("test", () -> is);
        assertNotNull(third);
        assertTrue(third.equals(first) || third.equals(second));
    }

    @Test
    public void testRandomStringFromFile() throws Exception {

        List<String> read = List.of("toost", "bloost");
        String path = CoreTest.class.getClassLoader().getResource("core-test.txt").getPath();

        Core instance = new Core();
        String first = instance.randomStringFromFile(path);
        assertNotNull(first);
        assertTrue(read.contains(first));

        String second = instance.randomStringFromFile(path);
        assertNotNull(second);
        assertNotEquals(first, second);
        assertTrue(read.contains(second));

        String third = instance.randomStringFromFile(path);
        assertNotNull(third);
        assertTrue(third.equals(first) || third.equals(second));
    }

    @Test
    public void testRandomStringFromNonExistentFile() throws Exception {
        Core instance = new Core();
        assertThrows(IOException.class, () -> {
            instance.randomStringFromFile("no-hay");
        });
    }

    @Test
    public void testRandomDate() throws Exception {
        final Core test = new Core();
        final String dateStart = "1910-01-01";
        final String dateEnd = "1930-01-01";
        final String format = "yyyy-MM-dd";
        System.out.println("Testing random date generation between 1910-01-01 and 1930-01-01");

        String rand = test.randomDate(dateStart, dateEnd, format);
        assertNotNull(rand);
        assertFalse(rand.isEmpty());
        System.out.println("Generated random date: " + rand);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        LocalDate ds = LocalDate.parse(dateStart, fmt);
        LocalDate de = LocalDate.parse(dateEnd, fmt);
        LocalDate rd = LocalDate.parse(rand, fmt);

        assertNotNull(rd);
        assertTrue(rd.isAfter(ds) || rd.isEqual(ds));
        assertTrue(rd.isBefore(de) || rd.isEqual(de));
    }

    @Test
    public void testRandomDateTime() throws Exception {
        final Core test = new Core();
        final String dateStart = "1980-01-01 00:00:00";
        final String dateEnd = "2020-01-01 12:22:33";
        final String format = "yyyy-MM-dd HH:mm:ss";
        System.out.println("Testing random date/time generation between 1980-01-01 00:00:00 and 2020-01-01 12:22:33");

        String rand = test.randomDateTime(dateStart, dateEnd, format);
        assertNotNull(rand);
        assertFalse(rand.isEmpty());
        System.out.println("Generated random date/time: " + rand);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        LocalDateTime ds = LocalDateTime.parse(dateStart, fmt);
        LocalDateTime de = LocalDateTime.parse(dateEnd, fmt);
        LocalDateTime rd = LocalDateTime.parse(rand, fmt);

        assertNotNull(rd);
        assertTrue(rd.isAfter(ds) || rd.isEqual(ds));
        assertTrue(rd.isBefore(de) || rd.isEqual(de));
    }

    @Test
    public void testRandomString() {
        final Core test = new Core();
        int num = 10;
        int length = 200;   // make sure 10 words fits

        Set<String> results = Set.of(
            test.randomString(num, length),
            test.randomString(num, length),
            test.randomString(num, length),
            test.randomString(num, length),
            test.randomString(num, length)
        );
        assertTrue(results.size() == 5);
        for (String res : results) {
            assertTrue(res.length() <= 200, "\"" + res + "\" is longer than 200 characters in length");
            assertTrue(StringUtils.countMatches(res, ' ') == 9, "\"" + res + "\" doesn't have 9 spaces");
            assertTrue(res.length() > 20, "\"" + res + "\" is less than 20 characters in length");
        }
    }

    @Test
    public void testRandomStringFromPattern() {
        System.out.println("randomStringFromPattern");
        String regex = "[0-9]{3}-[A-Za-z]{3}-[0-9]{3}";
        Core test = new Core();
        String result = test.randomStringFromPattern(regex);
        assertNotNull(result);
        assertTrue(result.matches(regex));
    }
}
