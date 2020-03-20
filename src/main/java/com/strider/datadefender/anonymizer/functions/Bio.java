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

import com.strider.datadefender.requirement.functions.RequirementFunctionClass;
import com.strider.datadefender.utils.Xeger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class Bio extends Core {

    private static final Random rand = new Random();

    public String randomFirstName() throws IOException {
		return randomStringFromStream(
            "resource:first_names.txt",
            () -> Lipsum.class.getClassLoader().getResourceAsStream("first_names.txt")
        );
    }

    public String randomLastName() throws IOException {
        return randomStringFromStream(
            "resource:last_names.txt",
            () -> Lipsum.class.getClassLoader().getResourceAsStream("last_names.txt")
        );
    }

    public String randomMiddleName(final String file) throws IOException {
        return randomFirstName();
    }

    public String randomEmail(final String domainName) {
        final StringBuilder email = new StringBuilder();
        email.append(randomString(1, 43).trim().toLowerCase()).append('@').append(domainName);
        return email.toString();
    }
}
