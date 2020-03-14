/**
 * Copyright 2014-2020, Armenak Grigoryan, and individual contributors as indicated
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

package com.strider.datadefender.specialcase;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

import org.apache.commons.validator.EmailValidator;

import com.strider.datadefender.utils.CommonUtils;
import com.strider.datadefender.database.metadata.TableMetaData;

/**
 * @author Armenak Grigoryan
 */
public class EmailDetector implements SpecialCase {
    private static final Logger LOG = getLogger(EmailDetector.class);
    
    public static TableMetaData detectEmail(final TableMetaData metaData, final String text) {
        String emailValue = "";
        
        if (!CommonUtils.isEmptyString(text)) {
            emailValue = text;
        }

        if (isValidEmail(emailValue)) {
                LOG.debug("Email detected: " + emailValue);
                metaData.setAverageProbability(1.0);
                metaData.setModel("email");
                return metaData;
        } else {
            LOG.debug("Email " + emailValue + " is not valid" );
        }

        return null;
    }    
    
    /**
     * @param email Email address
     * @return true if email is valid, otherwise false
     */
    private static boolean isValidEmail(final String email) {

        EmailValidator eValidator = EmailValidator.getInstance();
        if (eValidator.isValid(email)) {
            LOG.debug("*************** Email " + email + " is valid");
            return true;
        } else {
            return false;
        }
    }
}