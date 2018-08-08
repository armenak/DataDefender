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

package com.strider.datadefender.extensions;

import com.strider.datadefender.functions.CoreFunctions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Matthew Eaton
 */
public class BiographicFunctions extends CoreFunctions {

    private static final Logger log = getLogger(BiographicFunctions.class);
    
    /**
     * Generates random 9-digit social insurance number
     * @return String
     * @throws java.text.ParseException
     */
    public java.sql.Date randomBirthDate() throws java.text.ParseException{

        final GregorianCalendar gc = new GregorianCalendar();
        final int year = randBetween(1900, 2016);
        gc.set(GregorianCalendar.YEAR, year);
        final int dayOfYear = randBetween(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gc.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
        
        final String birthDate = prependZero(gc.get(GregorianCalendar.DAY_OF_MONTH)) + "-" + 
               prependZero(gc.get(GregorianCalendar.MONTH) + 1) + "-" + 
               gc.get(GregorianCalendar.YEAR);
        log.debug("BirthDate:[" + birthDate +"]");
        
        final DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        final java.sql.Date date = new java.sql.Date(format.parse(birthDate).getTime());
       
        log.debug("Generated BirthDate:[" + date.toString() +"]");

        return date;

    }
    
    private static String prependZero(final int num) {
        String dayStr;
        if (num <=9) {
            dayStr = '0' + String.valueOf(num);
        } else {
            dayStr = String.valueOf(num);
        }
        return dayStr;
    }

    private static int randBetween(final int start, final int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }    

    /**
     * Algorithm is taken from https://en.wikipedia.org/wiki/Social_Insurance_Number
     * @param sin
     * @return boolean true, if SIN is valid, otherwise false
     */
    public static boolean isValidSIN(final String sin) {
        boolean valid = false;
        
        if (sin.length() < 9) {
            log.debug("SIN length is < 9");
            return valid;
        }
        
        if (!StringUtils.isNumeric(sin)) {
            log.debug("SIN " + sin + " is not number");
            return valid;
        }
        

        final int[] sinArray = new int[sin.length()];
        final int[] checkArray = {1,2,1,2,1,2,1,2,1};
        final List<Integer> sinList = new ArrayList();
        
        log.info(sin);
        
        for (int i=0;i<9;i++) {
            sinArray[i] = Integer.valueOf(sin.substring(i,i+1));
            sinArray[i] = sinArray[i]*checkArray[i];
        }        
        
        int sum = 0;
        for (int i=0;i<9;i++) {
            final String tmp = String.valueOf(sinArray[i]);
            if (tmp.length()==1) {
                sinList.add(Integer.valueOf(tmp));
                sum += Integer.valueOf(tmp);
            } else {
                sinList.add(Integer.valueOf(tmp.substring(0,1)));
                sum += Integer.valueOf(tmp.substring(0,1));
                sinList.add(Integer.valueOf(tmp.substring(1,2)));                
                sum += Integer.valueOf(tmp.substring(1,2));
            }
        }          
        
        if ( (sum % 10) == 0) {
            valid = true;
        }        
        
        return valid;
    }
    
    
    /**
     * Check if String represents a valid sin
     * @param sin String representing a sin
     * @return true if valid sin, false if not
     */
//    public boolean isValidSIN(final String sin) {
//        if (sin == null || sin.length() != 9 ) {
//            log.info("isValidSin: Step 0");
//            return false;
//        }
//        int[] sinDigits = new int[sin.length()];
//        log.info("isValidSin: Step 1");
//                    
//        for (int i=0; i<sin.length(); i++) {
//            sinDigits[i] = Integer.parseInt(sin.substring(i, i+1));
//        }
//
//        return (isValidSIN(sinDigits));
//    }
}