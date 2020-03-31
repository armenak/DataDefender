/*
 * Copyright 2014, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.datadefender.requirement.plan;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

import lombok.extern.log4j.Log4j2;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author Zaahid Bateson
 */
@Log4j2
@Getter
@Setter
@XmlAccessorType(XmlAccessType.NONE)
public class GlobalPlan extends Plan {

    @XmlID
    @XmlAttribute
    private String id;

    /**
     * Uses functionName and parameters to find the method to associate with
     * 'Function'.
     *
     * @param unmarshaller
     * @param parent
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Override
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
        throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        InvocationTargetException {

        log.debug("Unmarshalling GlobalPlan with id {}", id);
        log.debug("Number of functions: {}", CollectionUtils.size(getFunctions()));
        // do nothing
    }
}
