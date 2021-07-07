package com.strider.datadefender.anonymizer.functions;

import com.strider.datadefender.utils.Encoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zaahid Bateson <zaahid.bateson@ubc.ca>
 */
@Log4j2
public class BioTest {
    @Test
    public void testRandomUser() {
        final Bio test = new Bio();
        final int maxCharacters = 10;
        final int numDigits = 2;
        final Pattern pattern = Pattern.compile("^([a-z]+)(\\d+)$");

        log.debug("Generating 50 random user names");
        for (int i = 0; i < 50; ++i) {
            String generatedUser = test.randomUser(maxCharacters, numDigits);
            log.debug(generatedUser);
            Matcher m = pattern.matcher(generatedUser);
            assertTrue(m.matches());
            assertTrue(m.group(1).length() <= maxCharacters);
            assertTrue(m.group(2).length() == numDigits);
        }
    }

    @Test
    public void testRandomUserNoDigits() {
        final Bio test = new Bio();
        final int maxCharacters = 10;
        final int numDigits = 0;
        final Pattern pattern = Pattern.compile("^([a-z]+)$");
        log.debug("Generating random user name without digits");

        String generatedUser = test.randomUser(maxCharacters, numDigits);
        log.debug(generatedUser);
        Matcher m = pattern.matcher(generatedUser);
        assertTrue(m.matches());
        assertTrue(m.group(1).length() <= maxCharacters);
    }

    @Test
    public void testRandomLongUser() {
        final Bio test = new Bio();
        final int maxCharacters = 20;
        final int numDigits = 2;
        final Pattern pattern = Pattern.compile("^([a-z]+)(_[a-z]+)?(\\d+)$");

        log.debug("Generating 50 long random user names");
        for (int i = 0; i < 50; ++i) {
            String generatedUser = test.randomUser(maxCharacters, numDigits);
            log.debug(generatedUser);
            Matcher m = pattern.matcher(generatedUser);
            assertTrue(m.matches());
            assertTrue(m.group(1).length() + ObjectUtils.defaultIfNull(m.group(2), "").length() <= maxCharacters);
            assertTrue(m.group(3).length() == numDigits);
        }
    }

    @Test
    public void testRandomEmail() {
        final Bio test = new Bio();
        final int maxCharacters = 20;
        final int numDigits = 2;
        final Pattern pattern = Pattern.compile("^([a-z]+)(\\.[a-z]+)?(\\d+)?@(domain.com)$");

        log.debug("Generating 50 random user emails");
        for (int i = 0; i < 50; ++i) {
            String generatedEmail = test.randomEmail("domain.com");
            log.debug(generatedEmail);
            Matcher m = pattern.matcher(generatedEmail);
            assertTrue(m.matches());
            assertTrue(m.group(1).length() + ObjectUtils.defaultIfNull(m.group(2), "").length() <= maxCharacters);
            assertTrue(ObjectUtils.defaultIfNull(m.group(3), "").length() <= numDigits);
            assertEquals("domain.com", m.group(4));
        }
    }

    @Test
    public void testRandomShortEmailTwoDigits() {
        final Bio test = new Bio();
        final int maxCharacters = 10;
        final int numDigits = 2;
        final Pattern pattern = Pattern.compile("^([a-z]+)(\\d+)@(domain.com)$");

        log.debug("Generating 50 random short user emails with two digits");
        for (int i = 0; i < 50; ++i) {
            String generatedEmail = test.randomEmail("domain.com", maxCharacters, numDigits);
            log.debug(generatedEmail);
            Matcher m = pattern.matcher(generatedEmail);
            assertTrue(m.matches());
            assertTrue(m.group(1).length() <= maxCharacters);
            assertTrue(m.group(2).length() == numDigits);
            assertEquals("domain.com", m.group(3));
        }
    }
    
    @Test
    public void testRandomFirstName() {
        final Bio bio = new Bio();
        
        final String encryptedValue = bio.randomFirstName("Armenak Grigoryan");
        log.debug("Encrypted value: " + encryptedValue);
        
        final String decryptedValue = new Encoder().decrypt(encryptedValue, bio.getHash());
        log.debug("Decrypted value: " + decryptedValue);
        
        assertEquals("Armenak Grigoryan", decryptedValue);
    }
}
