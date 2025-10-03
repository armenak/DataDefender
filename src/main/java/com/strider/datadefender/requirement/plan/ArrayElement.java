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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

import lombok.Data;

/**
 * JAXB class defining values for array parameters.
 *
 * Represents the value of a single array element for a given Parameter.
 *
 * @see Parameter.getArrayElements
 * @author Zaahid Bateson
 */
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class ArrayElement {
    /**
     * The array element's value
     */
    @XmlAttribute
    private String value;

    @XmlAttribute(name = "pass-current-value")
    private boolean isDynamicValue = false;
}
