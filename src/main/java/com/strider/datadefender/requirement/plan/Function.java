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

import com.strider.datadefender.functions.NamedParameter;
import com.strider.datadefender.requirement.TypeConverter;
import com.strider.datadefender.requirement.registry.ClassAndFunctionRegistry;
import com.strider.datadefender.requirement.registry.RequirementFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Zaahid Bateson
 */
@Log4j2
@Data
@XmlAccessorType(XmlAccessType.NONE)
public class Function implements Invokable {

    @Setter(AccessLevel.NONE)
    @XmlAttribute(name = "name")
    private String functionName;

    private Method function;

    @XmlAttribute(name = "combiner-glue")
    private String combinerGlue;

    private Object combinerGlueObject;

    @XmlElement(name = "argument")
    private List<Argument> arguments;

    private boolean isCombinerFunction = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ClassAndFunctionRegistry registry;

    public Function() {
        this(ClassAndFunctionRegistry.singleton());
    }

    public Function(ClassAndFunctionRegistry registry) {
        this.registry = registry;
    }

    public Function(String functionName, boolean isCombinerFunction) {
        this();
        this.functionName = functionName;
        this.isCombinerFunction = isCombinerFunction;
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
     * Returns true if the underlying method is static
     */
    public boolean isStatic() {
        return Modifier.isStatic(function.getModifiers());
    }

    /**
     * Looks for a class/method in the passed Function parameter in the form
     * com.package.Class#methodName, com.package.Class.methodName, or
     * com.package.Class::methodName.
     *
     * @return
     * @throws ClassNotFoundException
     */
    private List<Method> getFunctionCandidates(Class<?> returnType) 
        throws ClassNotFoundException {

        int index = StringUtils.lastIndexOfAny(functionName, "#", ".", "::");
        if (index == -1) {
            throw new IllegalArgumentException(
                "Function element is empty or incomplete: " + functionName
            );
        }

        String cn = functionName.substring(0, index);
        String fn = StringUtils.stripStart(functionName.substring(index), "#.:");
        int argCount = CollectionUtils.size(arguments);

        log.debug("Looking for function in class {} with name {} and {} parameters", cn, fn, argCount);
        Class<?> clazz = registry.getClassForName(cn);
        List<Method> methods = Arrays.asList(clazz.getMethods());

        return methods
            .stream()
            .filter((m) -> {
                if (!StringUtils.equals(fn, m.getName()) || !Modifier.isPublic(m.getModifiers())) {
                    return false;
                }
                final int ac = (!isCombinerFunction || Modifier.isStatic(m.getModifiers())) ? argCount : 1;
                log.debug(
                    "Candidate function {} needs {} parameters and gives {} return type, "
                    + "looking for {} arguments and {} return type",
                    () -> m.getName(),
                    () -> m.getParameterCount(),
                    () -> m.getReturnType(),
                    () -> ac,
                    () -> returnType
                );
                return (m.getParameterCount() == ac
                    && TypeConverter.isConvertible(m.getReturnType(), returnType));
            })
            .collect(Collectors.toList());
    }

    /**
     * 
     * @param type 
     */
    private void initializeCombinerGlue(Class<?> type)
        throws InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
        if (!isCombinerFunction && combinerGlue != null) {
            log.debug("Converting combinerGlue {} to object of type {}", combinerGlue, type);
            combinerGlueObject = TypeConverter.convert(combinerGlue, type);
        }
    }

    /**
     * Finds a method in the passed candidates with parameters matching the
     * arguments assigned to the current object, and returns it, or null if not
     * found.
     *
     * @param candidates
     * @return
     */
    private Method findCandidateFunction(List<Method> candidates) {
        final Map<String, Argument> mappedArgs = CollectionUtils.emptyIfNull(arguments).stream()
            .collect(Collectors.toMap(Argument::getName, (o) -> o, (x, y) -> y));
        return candidates.stream().filter((m) -> {
            int index = -1;
            for (java.lang.reflect.Parameter p : m.getParameters()) {
                ++index;
                Argument arg = arguments.get(index);
                NamedParameter named = p.getAnnotation(NamedParameter.class);
                if (named != null && mappedArgs.containsKey(named.value())) {
                    arg = mappedArgs.get(named.value());
                } else if (mappedArgs.containsKey(p.getName())) {
                    arg = mappedArgs.get(p.getName());
                }
                if (arg == null || !TypeConverter.isConvertible(p.getType(), arg.getType())) {
                    return false;
                }
            }
            return true;
        }).sorted((a, b) -> {
            int score = 0;
            java.lang.reflect.Parameter[] aps = a.getParameters();
            java.lang.reflect.Parameter[] bps = b.getParameters();
            log.debug("Sorting method: {}", a.getName());
            for (int i = 0; i < aps.length; ++i) {
                Argument arg = arguments.get(i);
                NamedParameter named = aps[i].getAnnotation(NamedParameter.class);
                if (named != null && mappedArgs.containsKey(named.value())) {
                    arg = mappedArgs.get(named.value());
                } else if (mappedArgs.containsKey(aps[i].getName())) {
                    arg = mappedArgs.get(aps[i].getName());
                }
                int s = TypeConverter.compareConversion(arg.getType(), aps[i].getType(), bps[i].getType());
                log.debug("Comparing arguments for sorting: {} with: {} and: {}, result: {}", arg.getType(), aps[i].getType(), bps[i].getType(), s);
                score += s;
            }
            log.debug("Score between {}, {}: {}", a, b, score);
            return score;
        }).findFirst().orElse(null);
    }

    /**
     * Specialized function finder for a combiner that looks for either a single
     * parameter for a non-static function that would be run on the first
     * argument, or a two-parameter static function with compatible types.
     *
     * @param candidates
     * @return
     */
    private Method findCombinerCandidateFunction(List<Method> candidates) {
        return candidates.stream().filter((m) -> {
            boolean isStatic = Modifier.isStatic(m.getModifiers());
            int count = m.getParameterCount();
            if (count == 0 || (isStatic && count != 2) || (!isStatic && count != 1)) {
                return false;
            }
            int index = -1;
            if (!isStatic && !RequirementFunction.class.isAssignableFrom(m.getDeclaringClass())) {
                ++index;
                if (!TypeConverter.isConvertible(arguments.get(index).getType(), m.getDeclaringClass())) {
                    return false;
                }
            }
            for (java.lang.reflect.Parameter p : m.getParameters()) {
                ++index;
                Argument arg = arguments.get(index);
                if (!TypeConverter.isConvertible(p.getType(), arg.getType())) {
                    return false;
                }
            }
            return true;
        }).findFirst().orElse(null);
    }

    /**
     * Uses functionName and arguments to find the method to associate with
     * 'Function'.
     *
     * @param returnType
     */
    Method initialize(Class<?> returnType)
        throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {

        log.debug("Initializing function {}", functionName);
        initializeCombinerGlue(returnType);
        List<Method> candidates = getFunctionCandidates(returnType);
        log.debug(
            "Found method candidates: {}",
            () -> CollectionUtils.emptyIfNull(candidates).stream()
                .map((m) -> m.getName() + " " + m.getParameterCount())
                .collect(Collectors.toList())
        );
        if (!isCombinerFunction) {
            function = findCandidateFunction(candidates);
        } else {
            function = findCombinerCandidateFunction(candidates);
        }

        log.debug("Function references method: {}", () -> (function == null) ? "null" : function);
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
    @Override
    public Object invoke(Object lastValue)
        throws SQLException,
        IllegalAccessException,
        InvocationTargetException,
        InstantiationException {

        log.debug("Function declaring class: {}", function.getDeclaringClass());
        log.debug("Function: {}", function);
        ClassAndFunctionRegistry registry = ClassAndFunctionRegistry.singleton();
        Object ob = registry.getFunctionsSingleton(function.getDeclaringClass());
        if (
            ob == null
            && lastValue != null
            && !Modifier.isStatic(function.getModifiers())
            && !RequirementFunction.class.isAssignableFrom(function.getDeclaringClass())
            && TypeConverter.isConvertible(lastValue.getClass(), function.getDeclaringClass())
        ) {
            ob = TypeConverter.convert(lastValue, function.getDeclaringClass());
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
            log.debug("Looking for argument {} in {} or {} in {}", p.getName(), mappedArgs, index, arguments);
            Argument arg = arguments.get(index);
            NamedParameter named = p.getAnnotation(NamedParameter.class);
            if (named != null && mappedArgs.containsKey(named.value())) {
                arg = mappedArgs.get(named.value());
            } else if (mappedArgs.containsKey(p.getName())) {
                arg = mappedArgs.get(p.getName());
            }
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
