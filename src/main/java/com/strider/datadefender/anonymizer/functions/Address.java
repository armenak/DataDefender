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

import com.strider.datadefender.utils.Xeger;

import java.io.IOException;

import org.apache.commons.lang3.RandomUtils;

import lombok.extern.log4j.Log4j2;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class Address extends Core {

    public String randomCountry() throws IOException {
        return randomStringFromStream(
            "resource:countries.txt",
            () -> Lipsum.class.getClassLoader().getResourceAsStream("countries.txt")
        );
    }

    public String randomCity() throws IOException {
        return randomStringFromStream(
            "resource:cities.txt",
            () -> Lipsum.class.getClassLoader().getResourceAsStream("cities.txt")
        );
    }

    public String randomStreet() throws IOException {
        return randomStringFromStream(
            "resource:streets.txt",
            () -> Lipsum.class.getClassLoader().getResourceAsStream("streets.txt")
        );
    }

    public String randomProvinceState()  throws IOException {
        return randomStringFromStream(
            "resource:provinces_states.txt",
            () -> Lipsum.class.getClassLoader().getResourceAsStream("provinces_states.txt")
        );
    }

    public String randomProvinceStateCode()  throws IOException {
        return randomStringFromStream(
            "resource:provinces_states_codes.txt",
            () -> Lipsum.class.getClassLoader().getResourceAsStream("provinces_states_codes.txt")
        );
    }

    public String randomCanadianPostalCode() {
        final Xeger instance = new Xeger("[A-Z][0-9][A-Z] [0-9][A-Z][0-9]");
        return instance.generate();
    }

    public String randomUsZipCode() {
        final Xeger instance = new Xeger("[0-9]{5}");
        return instance.generate();
    }

    public String randomUsZipCodeNineDigit() {
        final Xeger instance = new Xeger("[0-9]{5}-[0-9]{4}");
        return instance.generate();
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
