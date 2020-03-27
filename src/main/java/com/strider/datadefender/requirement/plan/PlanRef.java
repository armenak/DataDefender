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

import com.strider.datadefender.requirement.Column;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

import lombok.extern.log4j.Log4j2;
import lombok.Data;

/**
 *
 * @author Zaahid Bateson
 */
@Log4j2
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class PlanRef {

    @XmlIDREF
    @XmlAttribute(name = "ref-id")
    private GlobalPlan ref;

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
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
        throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        InvocationTargetException {

        log.debug("Unmarshalling plan-ref");
        log.debug("ref-id: {}", () -> ref.getId());

        if (!(parent instanceof Column)) {
            return;
        }
        final Class<?> columnType = ((Column) parent).getType();
        ref.initialize(columnType);
    }
}
