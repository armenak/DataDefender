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
 */
package com.strider.datadefender.specialcase;

import com.strider.datadefender.discoverer.Discoverer.ColumnMatch;
import org.apache.commons.validator.routines.EmailValidator;

import com.strider.datadefender.database.metadata.TableMetaData;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Armenak Grigoryan
 */
@Log4j2
public class EmailDetector implements SpecialCase {

    public static ColumnMatch detectEmail(final ColumnMetaData metaData, final String text) {
        if (StringUtils.isNotBlank(text) && isValidEmail(text)) {
                log.debug("Email detected: " + text);
                return new ColumnMatch(metaData, 1.0, "email", null);
        } else {
            log.debug("Email " + text + " is not a valid email");
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
            log.debug("*************** Email " + email + " is valid");
            return true;
        } else {
            return false;
        }
    }
}
