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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;

import lombok.extern.log4j.Log4j2;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author Zaahid Bateson
 */
@Log4j2
@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
public class GlobalPlan extends Plan {

    @XmlID
    @XmlAttribute
    private String id;

    public GlobalPlan(String id) {
        this.id = id;
    }

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
