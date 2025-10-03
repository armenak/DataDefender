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
package com.strider.datadefender.requirement;

import com.strider.datadefender.requirement.plan.Plan;
import com.strider.datadefender.requirement.plan.PlanRef;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import static java.util.Collections.unmodifiableList;
import org.apache.commons.beanutils.ConvertUtils;

/**
 * JAXB class that defines column elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@Log4j2
@Data
@XmlAccessorType(XmlAccessType.NONE)
public class Column {

    @XmlAttribute
    private String name;

    @XmlAttribute(name = "skip-empty")
    private boolean ignoreEmpty = true;

    @XmlJavaTypeAdapter(ClassAdapter.class)
    @XmlAttribute
    private Class<?> type = String.class;

    @XmlElement
    private Plan plan;

    @XmlElement(name = "plan-ref")
    private PlanRef planRef;

    @XmlElementWrapper(name = "exclusions")
    @XmlElement(name = "exclude")
    private List<Exclude> exclusions;

    public Column() {
    }

    public Column(String name) {
        this.name = name;
    }

    /**
     * Returns a list of exclusions
     *
     * @return List<Exclude>
     */
    public List<Exclude> getExclusions() {
        final List<Exclude> lst = new ArrayList<>();
        if (!CollectionUtils.isEmpty(exclusions)) {
            lst.addAll(exclusions);
        }
        if (ignoreEmpty) {
            lst.add(new Exclude(true));
        }
        return unmodifiableList(lst);
    }

    /**
     * Returns the plan used by this column, either the one defined under it,
     * or the one referenced by planRef.
     *
     * @return
     */
    public Plan getResolvedPlan() {
        if (planRef != null) {
            return planRef.getRef();
        }
        return plan;
    }

    /**
     * Calls all functions defined under Functions in order.
     *
     * @param rs ResultSet
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws java.lang.InstantiationException
     */
    public Object invokeFunctionChain(ResultSet rs)
        throws SQLException,
        IllegalAccessException,
        InvocationTargetException,
        InstantiationException {

        Object startingValue = null;
        Class<?> argType = getResolvedPlan().getDynamicArgumentType();
        if (argType != null && ClassUtils.isAssignable(ResultSet.class, argType)) {
            startingValue = rs;
        } else {
            startingValue = rs.getObject(name, type);
        }
        return ConvertUtils.convert(getResolvedPlan().invoke(startingValue), type);
    }
}
