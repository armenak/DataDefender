/*
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package com.strider.datadefender.anonymizer.functions;

import com.strider.datadefender.functions.NamedParameter;
import com.strider.datadefender.utils.Encoder;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;

/**
 * Built-in anonymization helper functions for personal bio data.
 *
 * @author Armenak Grigoryan
 */
@Log4j2
public class Bio extends Core {

    public String randomFirstName(final String firstName) 
    throws Exception {
        
        final Encoder encoder = new Encoder();
        String detFirstName = encoder.encode(firstName);
        
        return detFirstName;
    }
    
//    public String randomFirstName() throws IOException {
//		return randomStringFromStream(
//            "resource:first_names.txt",
//            () -> Lipsum.class.getResourceAsStream("first_names.txt")
//        );
//    }

    public String randomLastName() throws IOException {
        return randomStringFromStream(
            "resource:last_names.txt",
            () -> Lipsum.class.getResourceAsStream("last_names.txt")
        );
    }

//    public String randomMiddleName(final String file) throws IOException {
//        return randomFirstName();
//    }

    /**
     * Creates a random username with up to 10 characters and between 0 and 2
     * digits following.
     *
     * @return
     */
    public String randomUser() {
        return randomUser(10, RandomUtils.nextInt(0, 3));
    }

    /**
     * Generates a random user of up to maxChars length and followed by
     * numDigits digits.
     *
     * For each '10' chars, an additional word is generated on a random basis
     * to make up the user name. So a request for a username with 11 characters
     * may be either a single word, 'user' or maybe two, separated by a "_":
     * 'user_name'.
     *
     * @param maxCharacters
     * @param numDigits
     * @return
     */
    public String randomUser(@NamedParameter("maxCharacters") int maxCharacters, @NamedParameter("numDigits") int numDigits) {
        int maxWords = (int) Math.ceil(maxCharacters / 10d);
        int numWords = (maxWords > 1) ? RandomUtils.nextInt(1, maxWords + 1) : 1;
        String user = randomString(numWords, maxCharacters).toLowerCase().replaceAll("[^a-z ]", "").replace(" ", "_");
        String digits = (numDigits > 0) ? Integer.toString(RandomUtils.nextInt(0, (int) Math.pow(10, numDigits))) : "";
        return user + ("0".repeat(numDigits) + digits).substring(digits.length());
    }

    /**
     * Creates an email with a user part up to 20 characters long, and between
     * 0 and 2 digits following, followed by an '@' character, and the provided
     * domainName.
     *
     * @param domainName
     * @return
     */
    public String randomEmail(@NamedParameter("domainName") String domainName) {
        return randomEmail(domainName, 20, RandomUtils.nextInt(0, 3));
    }

    /**
     * Uses the randomUser method to create the user portion of an email with
     * the passed maxUserCharacters and numDigits, but replaces returned '_'
     * characters with '.'.
     *
     * @param domainName
     * @param maxUserCharacters
     * @param numDigits
     * @return
     */
    public String randomEmail(@NamedParameter("domainName") String domainName, @NamedParameter("maxUserCharacters") int maxUserCharacters, @NamedParameter("numDigits") int numDigits) {
        String user = randomUser(maxUserCharacters, numDigits).replace("_", ".");
        return user + "@" + domainName;
    }
}
