/*
 * Copyright (C) 2022 Zaahid Bateson.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.strider.datadefender.requirement.file;


import org.w3c.dom.Node;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.Level;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author Zaahid Bateson
 */
@Log4j2
public class JaxbValidationEventHandler implements ValidationEventHandler {
    
    @Override
    public boolean handleEvent(ValidationEvent event) {
        
        if (event == null) {
            throw new IllegalArgumentException();
        }
        
        Map<Integer, Level> severityToLevel = Map.of(
            ValidationEvent.WARNING, Level.WARN,
            ValidationEvent.ERROR, Level.ERROR,
            ValidationEvent.FATAL_ERROR, Level.FATAL
        );
        
        Level level = severityToLevel.getOrDefault(event.getSeverity(), Level.ERROR);
        String location = getFormattedLocation(event.getLocator());
        String message = event.getMessage();
        if (message == null && event.getLinkedException() != null) {
            message = ExceptionUtils.getRootCauseMessage(event.getLinkedException());
        }
        log.log(level, "{}\n\tLocation: {},{}", message, location, event.getLinkedException());
        return event.getSeverity() == ValidationEvent.WARNING;
    }

    private String getFormattedLocation(ValidationEventLocator locator) {
        
        String ret = "Location unavailable";
        if (locator != null) {
            
            ret = "";
            
            int line = locator.getLineNumber();
            URL url = locator.getURL();
            Object obj = locator.getObject();
            Node node = locator.getNode();
            
            if (line > -1) {
                ret += "Line: " + line;
                if (url != null) {
                    ret += " In: " + url;
                }
            }
            if (obj != null) {
                ret += " Object: " + obj;
            }
            if (node != null) {
                ret += " Node: " + node;
            }
        }
        
        return ret;
    }
}
