/*
 * 
 * Copyright 2014-2016, Armenak Grigoryan, Matthew Eaton, and individual contributors as indicated
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

package com.strider.dataanonymizer.extensions;

import com.strider.dataanonymizer.functions.CoreFunctions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import org.apache.commons.cli.ParseException;

/**
 * @author Matthew Eaton
 */
public class BiographicFunctions extends CoreFunctions {

    public BiographicFunctions() {
    }
    
    /**
     * Generates random 9-digit social insurance number
     * @return String
     */
    public String randomSIN() {
        Random random = new Random();
        int[] sinDigits = new int[9];
        for (int i = 0; i < sinDigits.length; i++) {
            sinDigits[i] = random.nextInt(9);
        }

        while (!isValidSIN(sinDigits)) {
            sinDigits = increment(sinDigits);
        }

        StringBuilder sin = new StringBuilder(9);
        for (int digit : sinDigits) {
            sin.append(String.valueOf(digit));
        }
        return sin.toString();

    }
    
    /**
     * Generates random 9-digit social insurance number
     * @return String
     * @throws org.apache.commons.cli.ParseException
     */
    public Date randomBirthDate() throws java.text.ParseException{

        GregorianCalendar gc = new GregorianCalendar();
        int year = randBetween(1900, 2016);
        gc.set(GregorianCalendar.YEAR, year);
        int dayOfYear = randBetween(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gc.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
        
        String birthDate = prependZero(gc.get(GregorianCalendar.DAY_OF_MONTH)) + "-" + 
               prependZero((gc.get(GregorianCalendar.MONTH) + 1)) + "-" + 
               gc.get(GregorianCalendar.YEAR);
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(birthDate);
        } catch (java.text.ParseException pe) {
            System.err.println(pe.toString());
        }
       
        return date;

    }
    
    private static String prependZero(int num) {
        String dayStr;
        if (num <=9) {
            dayStr = "0" + String.valueOf(num);
        } else {
            dayStr = String.valueOf(num);
        }
        return dayStr;
    }

    private static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }    

    /**
     * Increment array of integers by 1, starting from end of array
     * @param sinDigits Array of integers representing a sin
     * @return Incremented array of integers
     */
    private int[] increment(int[] sinDigits) {
        for (int i=8; i>=0; i--) {
            if (sinDigits[i] < 9) {
                sinDigits[i]++;
                break;
            } else {
                sinDigits[i] = 0;
            }
        }

        return sinDigits;
    }

    /**
     * Check if array of integers represents a valid sin
     * @param sinDigits Array of integers representing a sin
     * @return true if valid sin, false if not
     */
    private boolean isValidSIN(int[] sinDigits) {
        int check = 0;

        for (int i=0; i<sinDigits.length; i++) {
            if ((i % 2) == 0) {
                // multiple by 1
                check = check + sinDigits[i];
            } else {
                // multiple by 2
                int tempCheck = sinDigits[i] * 2;
                if (tempCheck > 9) {
                    tempCheck = (tempCheck / 10) + (tempCheck % 10);
                }
                check = check + tempCheck;
            }
        }
        return (check % 10) == 0;
    }

    /**
     * Check if String represents a valid sin
     * @param sin String representing a sin
     * @return true if valid sin, false if not
     */
    public boolean isValidSIN(String sin) {
        if (sin == null || sin.length() != 9 ) {
            return false;
        }
        int[] sinDigits = new int[sin.length()];

        for (int i=0; i<sin.length(); i++) {
            sinDigits[i] = Integer.parseInt(sin.substring(i, i+1));
        }

        return (isValidSIN(sinDigits));

    }
}