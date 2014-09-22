package com.strider.dataanonymizer.extensions;

import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.lang.String.valueOf;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import com.strider.dataanonymizer.functions.CoreFunctions;

/**
 * @author Armenak Grigoryan
 */
public class IntegratedScience extends CoreFunctions {
    
    private static Logger log = getLogger(IntegratedScience.class);

    public IntegratedScience() {
    }
    
    
    /**
     * Generates random 9-digit student number 
     * @return String
     */
    public String randomStudentNumber()  {
        return valueOf(round(random()*100000000));
    }    

}