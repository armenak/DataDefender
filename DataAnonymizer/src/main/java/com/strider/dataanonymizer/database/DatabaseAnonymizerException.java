package com.strider.dataanonymizer.database;

import com.strider.dataanonymizer.AnonymizerException;

/**
 * Package level exception. Extends application level exception.
 * @author Armenak Grigoryan
 */
public class DatabaseAnonymizerException extends AnonymizerException {
    private static final long serialVersionUID = 1L;
    
    public DatabaseAnonymizerException(String msg) {
        super(msg);
    }

    public DatabaseAnonymizerException(String msg, Throwable t) {
        super(msg, t);
    }    
}
