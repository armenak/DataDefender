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

import java.io.IOException;

import org.apache.commons.lang3.RandomUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Built-in anonymization helper functions for address field anonymization.
 *
 * @author Armenak Grigoryan
 */
@Log4j2
public class Address extends Core {

    public String randomCountry() throws IOException {
        String ret = randomStringFromStream(
            "resource:countries.txt",
            () -> Lipsum.class.getResourceAsStream("countries.txt")
        );
        log.info("Country: {}", ret);
        return ret;
    }

    public String randomCity() throws IOException {
        return randomStringFromStream(
            "resource:cities.txt",
            () -> Lipsum.class.getResourceAsStream("cities.txt")
        );
    }

    public String randomStreet() throws IOException {
        return randomStringFromStream(
            "resource:streets.txt",
            () -> Lipsum.class.getResourceAsStream("streets.txt")
        );
    }

    public String randomProvinceState()  throws IOException {
        return randomStringFromStream(
            "resource:provinces_states.txt",
            () -> Lipsum.class.getResourceAsStream("provinces_states.txt")
        );
    }

    public String randomProvinceStateCode()  throws IOException {
        return randomStringFromStream(
            "resource:provinces_states_codes.txt",
            () -> Lipsum.class.getResourceAsStream("provinces_states_codes.txt")
        );
    }

    public String randomCanadianPostalCode() {
        return randomStringFromPattern("[A-Z][0-9][A-Z] [0-9][A-Z][0-9]");
    }

    public String randomUsZipCode() {
        return randomStringFromPattern("[0-9]{5}");
    }

    public String randomUsZipCodeNineDigit() {
        return randomStringFromPattern("[0-9]{5}-[0-9]{4}");
    }

    public String randomCanadianOrUsFiveDigitPostalCode() {
        if (RandomUtils.nextBoolean()) {
            return randomCanadianPostalCode();
        }
        return randomUsZipCode();
    }

    public String randomCanadianOrUsFiveOrNineDigitPostalCode() {
        if (RandomUtils.nextBoolean()) {
            return randomCanadianPostalCode();
        } else if (RandomUtils.nextBoolean()) {
            return randomUsZipCode();
        
        }
        return randomUsZipCodeNineDigit();
    }
}
