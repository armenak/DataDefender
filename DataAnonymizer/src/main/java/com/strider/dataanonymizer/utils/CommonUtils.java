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

package com.strider.dataanonymizer.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Armenak Grigoryan
 */
public class CommonUtils {
    
    /**
     *
     * @param fileName
     * @return
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     */
    public static List<String> readStreamOfLines(String fileName) 
    throws IOException, FileNotFoundException, FileNotFoundException {
        List<String> names = new ArrayList<String>();
        Scanner s = new Scanner(new File(fileName));
        while (s.hasNext()){
            names.add(s.next());
        }
        s.close();
    
        return names;
   }    
    
    public static boolean isEmptyString(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        
        return false;
    }
}
