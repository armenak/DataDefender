/*
 *
 * Copyright 2015, Armenak Grigoryan, and individual contributors as indicated
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
 *
 */



package com.strider.datadefender.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.apache.log4j.Logger;

import static org.apache.log4j.Logger.getLogger;

/**
 * @author Armenak Grigoryan
 */
public class CommonUtils {
    private static final Logger log = getLogger(CommonUtils.class);

    public static String fixedLengthString(final char fillChar, final int count) {
        int cnt = count;

        // creates a string of 'x' repeating characters
        char[] chars = new char[cnt];

        while (cnt > 0) {
            chars[--cnt] = fillChar;
        }

        return new String(chars);
    }

    /**
     *
     * @param fileName
     * @return
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     */
    public static List<String> readStreamOfLines(final String fileName)
            throws IOException, FileNotFoundException, FileNotFoundException {
        final List<String> names = new ArrayList<>();
        final Scanner      s     = new Scanner(new File(fileName));

        while (s.hasNext()) {
            names.add(s.next());
        }

        s.close();

        return names;
    }

    public static java.sql.Date stringToDate(final String str, final String format) {
        final SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
        java.sql.Date          sqlDate   = null;

        try {
            final Date date = formatter.parse(str);

            sqlDate = new java.sql.Date(date.getTime());
        } catch (ParseException e) {
            log.error("Problem with parsing date");
        }

        return sqlDate;
    }

    public static boolean isEmptyString(final String str) {
        return (str == null) || str.isEmpty();
    }

    public static String getFileExtension(final File file) {
        final String fileName = file.getName();
        String       ret      = "";

        if ((fileName.lastIndexOf('.') != -1) && (fileName.lastIndexOf('.') != 0)) {
            ret = fileName.substring(fileName.lastIndexOf('.') + 1);
        }

        return ret;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
