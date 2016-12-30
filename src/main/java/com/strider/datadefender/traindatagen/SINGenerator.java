/*
 * 
 * Copyright 2014-2017, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.datadefender.traindatagen;

import com.strider.datadefender.extensions.BiographicFunctions;
import com.strider.datadefender.functions.CoreFunctions;
import java.io.PrintWriter;

/**
 * Generate valid list of social insurance numbers and random text in a format 
 * acceptable for OpenNLP data model trainer.
 * 
 * @author Armenak Grigoryan
 */
public class SINGenerator {
    
    private static final String START_TAG = "<START:sin>";
    private static final String END_TAG = "<END>";
    private static final String SPACE = " ";
    private static final int LINES = 20000;
    private static final String RANDOM_FILE = "/Users/strider/work/strider/DataDefender/src/main/resources/lipsum.txt";
    private static final String OUTOUT_FILE = "/Users/strider/work/strider/DataDefender/src/main/resources/sin.txt";    
    

    //<START:medicine> Augmentin-Duo <END> is a penicillin antibiotic that contains two medicines - <START:medicine> amoxicillin trihydrate <END> 
    // <START:medicine> potassium clavulanate <END>. They work together to kill certain types of bacteria and are used to treat certain types of bacterial infections
    public static void main(String[] args) throws Exception {
        
        final BiographicFunctions bf = new BiographicFunctions();
        final CoreFunctions cf = new CoreFunctions();
        final PrintWriter writer = new PrintWriter(OUTOUT_FILE, "UTF-8");
        
        for (int i=0; i<=LINES; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(START_TAG).append(SPACE).append(bf.randomStringSIN()).append(SPACE).append(END_TAG).append(SPACE).append(cf.randomStringFromFile(RANDOM_FILE)).append(SPACE).
            append(START_TAG).append(SPACE).append(bf.randomStringSIN()).append(SPACE).append(END_TAG).append(SPACE).append(cf.randomStringFromFile(RANDOM_FILE));
            writer.println(sb.toString());
        } 
        writer.flush();
    }
    
}
