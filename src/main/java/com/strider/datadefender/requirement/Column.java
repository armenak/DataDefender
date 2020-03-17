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

import com.strider.datadefender.requirement.functions.RequirementFunctionClassRegistry;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.unmodifiableList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * JAXB class that defines column elements in Requirement.xml file
 *
 * @author Armenak Grigoryan
 */
@Log4j2
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Column {

    @XmlAttribute(name = "Name")
    private String name;

    @XmlAttribute(name = "IgnoreEmpty")
    private boolean ignoreEmpty;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @XmlElement(name = "Function", required = true)
    private String functionName;

    private Method function;

    @XmlJavaTypeAdapter(ClassAdapter.class)
    @XmlElement(name = "ReturnType")
    private Class<?> returnType;

    @XmlElementWrapper(name = "Parameters")
    @XmlElement(name = "Parameter")
    private List<Parameter> parameters;

    @XmlElementWrapper(name = "Exclusions")
    @XmlElement(name = "Exclude")
    private List<Exclude> exclusions;

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
     * Setter for 'Function' element.
     *
     * @param fn
     */
    public void setFunction(Method fn) {
        function = fn;
        functionName = fn.getName();
    }

    /**
     * Looks for a class/method in the passed Function parameter in the form
     * com.package.Class#methodName, or for historical reasons,
     * com.package.Class.methodName.  Using "." for methodName is deprecated
     * however, and will be removed in a future version.
     *
     * @return
     * @throws ClassNotFoundException
     */
    private List<Method> findFunctionCandidates() throws ClassNotFoundException {
        int index = StringUtils.lastIndexOfAny(functionName, "#", ".");
        if (index == -1) {
            throw new IllegalArgumentException(
                "Function element is empty or incomplete: " + functionName
            );
        }
        if (functionName.charAt(index) == '.') {
            log.warn(
                "Using '.' as a method separator for a function is deprecated. "
                + "Please use \"#\" instead: {}",
                functionName
            );
        }
        String cn = functionName.substring(0, index);
        String fn = functionName.substring(index + 1);
        if (!cn.contains(".") && Character.isUpperCase(cn.charAt(0))) {
            cn = "java.lang." + cn;
        }
        Class clazz = ClassUtils.getClass(cn);
        List<Method> methods = Arrays.asList(clazz.getMethods());
            return methods
                .stream()
                .filter((m) -> Modifier.isPublic(m.getModifiers()) && StringUtils.equals(fn, m.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Runs the function referenced by the "Function" element and returns its
     * value.
     *
     * If the referenced function is non-static, and the passed object is null,
     * it attempts to retrieve the column's value from the passed ResultSet as
     * an Object of type "returnType", and tries to run it on the returned
     * object from the ResultSet.
     *
     * @param rs
     * @param ob
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object invokeFunction(ResultSet rs) throws SQLException, IllegalAccessException, InvocationTargetException {
        RequirementFunctionClassRegistry registry = RequirementFunctionClassRegistry.singleton();
        Object ob = registry.getFunctionsSingleton(function.getDeclaringClass());
        if (ob == null && !Modifier.isStatic(function.getModifiers()) && returnType.equals(function.getDeclaringClass())) {
            ob = rs.getObject(name, returnType);
        }
        final Map<String, Parameter> mappedParams = parameters.stream().collect(Collectors.toMap(Parameter::getName, (o) -> o));
        List<Object> fnArguments = new ArrayList<>();
        // because getValue(rs) throws SQLException, can't use stream map lambda function
        for (java.lang.reflect.Parameter p : function.getParameters()) {
            fnArguments.add(mappedParams.get(p.getName()).getValue(rs));
        }
        return function.invoke(ob, fnArguments.toArray());

    }

    /**
     * Uses functionName and parameters to find the method to associate with
     * 'Function'.
     *
     * @param unmarshaller
     * @param parent
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) throws Exception {
        List<Method> candidates = findFunctionCandidates();
        final Map<String, Parameter> mappedParams = parameters.stream().collect(Collectors.toMap(Parameter::getName, (o) -> o));
        function = candidates.stream().filter((m) -> {
            if (!m.getReturnType().equals(returnType) || m.getParameterCount() != mappedParams.size()) {
                return false;
            }
            return Arrays.stream(m.getParameters()).anyMatch((mp) -> {
                Parameter p = mappedParams.get(mp.getName());
                if (p == null || !ClassUtils.isAssignable(p.getType(), mp.getType()) || p.getValueAttribute() == null && !ClassUtils.isAssignable(null, mp.getType())) {
                    return false;
                }
                return true;
            });
        }).findFirst().orElse(null);
        if (function == null) {
            throw new IllegalArgumentException("Function maching signature and parameters not found");
        }
    }
}
