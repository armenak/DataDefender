package com.strider.dataanonymizer;

/**
 * Application-level exception
 * 
 * @author Armenak Grigoryan
 */

/**
 * Application-level exception
 * @author Armenak Grigoryan
 */
public class AnonymizerException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public AnonymizerException(String msg) {
        super(msg);
    }

    public AnonymizerException(String msg, Throwable t) {
        super(msg, t);
    }
}

