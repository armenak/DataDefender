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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author Zaahid Bateson
 */
@Log4j2
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Function {

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @XmlAttribute(name = "Name")
    private String functionName;
    private Method function;

    @XmlElement(name = "Argument")
    private List<Argument> arguments;

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
     * com.package.Class.methodName. Using "." for methodName is deprecated
     * however, and will be removed in a future version.
     *
     * @return
     * @throws ClassNotFoundException
     */
    private List<Method> getFunctionCandidates(Class<?> returnType) 
        throws ClassNotFoundException {

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
            .filter(
                (m) -> Modifier.isPublic(m.getModifiers())
                && StringUtils.equals(fn, m.getName())
                && TypeConverter.isConvertible(m.getReturnType(), returnType)
                && m.getParameterCount() == CollectionUtils.size(arguments)
            )
            .collect(Collectors.toList());
    }

    /**
     * Uses functionName and arguments to find the method to associate with
     * 'Function'.
     *
     * @param returnType
     */
    Method findFunction(Class<?> returnType) throws ClassNotFoundException {
        List<Method> candidates = getFunctionCandidates(returnType);
        final Map<String, Argument> mappedArgs = CollectionUtils.emptyIfNull(arguments).stream()
            .collect(Collectors.toMap(Argument::getName, (o) -> o, (x, y) -> y));
        function = candidates.stream().filter((m) -> {
            int index = -1;
            for (java.lang.reflect.Parameter p : m.getParameters()) {
                ++index;
                Argument arg = ((mappedArgs.containsKey(p.getName()))
                    || (p.isNamePresent() && arguments.get(0).getName() != null)) ?
                    mappedArgs.get(p.getName())
                    : arguments.get(index);
                if (arg == null || !TypeConverter.isConvertible(p.getType(), arg.getType())) {
                    return false;
                }
            }
            return true;
        }).findFirst().orElse(null);
        // could try and sort returned functions if more than one based on
        // "best selection" for argument/parameter types
        if (function == null) {
            throw new IllegalArgumentException("Function maching signature and arguments not found");
        }
        return function;
    }

    /**
     * Runs the function referenced by the "Function" element and returns its
     * value.
     *
     * @param lastValue
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object invokeFunction(Object lastValue)
        throws SQLException,
        IllegalAccessException,
        InvocationTargetException,
        InstantiationException {

        log.debug("Function declaring class: {}", function.getDeclaringClass());
        RequirementFunctionClassRegistry registry = RequirementFunctionClassRegistry.singleton();
        Object ob = registry.getFunctionsSingleton(function.getDeclaringClass());
        if (ob == null && lastValue != null && !Modifier.isStatic(function.getModifiers()) && lastValue.getClass().equals(function.getDeclaringClass())) {
            ob = lastValue;
        }
        java.lang.reflect.Parameter[] parameters = function.getParameters();
        List<Object> fnArguments = new ArrayList<>();
        final Map<String, Argument> mappedArgs = CollectionUtils.emptyIfNull(arguments).stream()
            .collect(Collectors.toMap(Argument::getName, (o) -> o, (x, y) -> x));
        // because getValue(rs) throws exceptions, can't use stream map
        // lambda function
        int index = -1;
        for (java.lang.reflect.Parameter p : parameters) {
            ++index;
            Argument arg = ((mappedArgs.containsKey(p.getName()))
                    || (p.isNamePresent() && arguments.get(0).getName() != null)) ?
                    mappedArgs.get(p.getName())
                    : arguments.get(index);
            fnArguments.add(TypeConverter.convert(
                arg.getValue(lastValue),
                p.getType()
            ));
        }
        final Object fnOb = ob;
        log.debug("invoking: {} on: {} with: ({})",
            () -> function.getName(),
            () -> fnOb,
            () -> fnArguments.toString()
        );
        return function.invoke(fnOb, fnArguments.toArray());
    }
}
