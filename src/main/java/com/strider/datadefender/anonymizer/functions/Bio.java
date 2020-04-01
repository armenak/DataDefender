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

import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class Bio extends Core {

    public String randomFirstName() throws IOException {
		return randomStringFromStream(
            "resource:first_names.txt",
            () -> Lipsum.class.getResourceAsStream("first_names.txt")
        );
    }

    public String randomLastName() throws IOException {
        return randomStringFromStream(
            "resource:last_names.txt",
            () -> Lipsum.class.getResourceAsStream("last_names.txt")
        );
    }

    public String randomMiddleName(final String file) throws IOException {
        return randomFirstName();
    }

    public String randomUser() {
        return randomUser(10, RandomUtils.nextInt(0, 3));
    }

    public String randomUser(@NamedParameter("maxCharacters") int maxChars, @NamedParameter("numDigits") int numDigits) {
        int maxWords = (int) Math.ceil(maxChars / 10d);
        int numWords = (maxWords > 1) ? RandomUtils.nextInt(1, maxWords + 1) : 1;
        String user = randomString(numWords, maxChars).toLowerCase().replaceAll("[^a-z ]", "").replace(" ", "_");
        String digits = (numDigits > 0) ? Integer.toString(RandomUtils.nextInt(0, (int) Math.pow(10, numDigits))) : "";
        return user + ("0".repeat(numDigits) + digits).substring(digits.length());
    }

    public String randomEmail(@NamedParameter("domainName") String domainName) {
        return randomEmail(domainName, 20, RandomUtils.nextInt(0, 3));
    }

    public String randomEmail(@NamedParameter("domainName") String domainName, @NamedParameter("maxUserCharacters") int maxUserChars, @NamedParameter("numDigits") int numDigits) {
        String user = randomUser(maxUserChars, numDigits).replace("_", ".");
        return user + "@" + domainName;
    }
}
