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
import com.strider.datadefender.requirement.TypeConverter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import lombok.extern.log4j.Log4j2;
import lombok.Data;

/**
 *
 * @author Zaahid Bateson
 */
@Log4j2
@Data
@XmlAccessorType(XmlAccessType.NONE)
public class Plan implements Invokable {

    @XmlJavaTypeAdapter(FunctionAttributeAdapter.class)
    @XmlAttribute
    private Function combiner;

    @XmlAttribute(name = "combiner-glue")
    private String combinerGlue;

    private Object combinerGlueObject;

    @XmlElement(name = "function")
    private List<Function> functions;

    /**
     * Returns the Class of the first function's dynamic argument if one is set,
     * or null otherwise.
     *
     * @return
     */
    public Class<?> getDynamicArgumentType() {
        if (CollectionUtils.size(functions) > 0) {
            return CollectionUtils.emptyIfNull(functions.get(0).getArguments())
                .stream()
                .filter((a) -> Objects.equals(Boolean.TRUE, a.getIsDynamicValue()))
                .map((a) -> a.getType())
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    /**
     * Invokes the combiner with the given arguments.
     *
     * If the combiner is non-static, assumes the combiner is a method defined
     * on firstArgs' class, and so calls it on firstArg, passing secondArg as an
     * argument.
     *
     * @param firstArg
     * @param secondArg
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    protected Object invokeCombiner(Object firstArg, Object secondArg)
        throws SQLException,
        IllegalAccessException,
        InvocationTargetException,
        InstantiationException {

        List<Argument> args = combiner.getArguments();
        int index = 0;
        if (combiner.isStatic()) {
            args.get(index++).setObjectValue(firstArg);
        }
        args.get(index).setObjectValue(secondArg);
        return combiner.invoke(firstArg);
    }

    /**
     * Chains the underlying functions, applying "Combiner" if set.
     *
     * @param runningValue
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    @Override
    public Object invoke(Object runningValue)
        throws SQLException,
        IllegalAccessException,
        InvocationTargetException,
        InstantiationException {

        boolean isFirst = true;
        Object glue = null;
        for (Function fn : functions) {
            log.debug("Invoking function: {}", fn.getFunction());
            Object returnValue = fn.invoke(runningValue);
            log.debug("runningValue = " + runningValue);
            log.debug("returnValue = " + returnValue);
            if (combiner != null && !isFirst) {
                final Object gl = glue;
                log.debug(
                    "Combining with: {}, using glue: \"{}\"",
                    () -> combiner.getFunctionName(),
                    () -> StringEscapeUtils.escapeJava(Objects.toString(gl))
                );
                if (gl != null) {
                    runningValue = invokeCombiner(runningValue, gl);
                }
                returnValue = invokeCombiner(runningValue, returnValue);
            }
            glue = ObjectUtils.firstNonNull(
                fn.getCombinerGlueObject(),
                combinerGlueObject
            );
            runningValue = returnValue;
            isFirst = false;
        }
        return runningValue;
    }

    protected void initialize(Class<?> columnType)
        throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        InvocationTargetException {

        log.debug("Initializing plan with column type: {}", columnType);
        Method cm = null;
        if (combiner != null) {
            log.debug(
                "Found combiner: {}, invoking for type: {}",
                () -> combiner.getFunctionName(),
                () -> columnType
            );
            combiner.setArguments(List.of(new Argument(columnType), new Argument(columnType)));
            cm = combiner.initialize(columnType);
            if (!StringUtils.isEmpty(combinerGlue)) {
                log.debug("Found combiner glue: \"{}\"", () -> StringEscapeUtils.escapeJava(combinerGlue));
                combinerGlueObject = TypeConverter.convert(combinerGlue, columnType);
                log.debug("combinerGlueObject: \"{}\"", () -> StringEscapeUtils.escapeJava(Objects.toString(combinerGlueObject)));
            }
        }

        for (Function fn : CollectionUtils.emptyIfNull(functions)) {
            Method m = fn.initialize(columnType);
            Class<?> rtype = m.getReturnType();
            if (cm != null && !TypeConverter.isConvertible(rtype, cm.getParameterTypes()[0])) {
                log.debug(
                    "The combiner {} cannot be called with the return type: {} of one of the methods in the chain: {}",
                    combiner.getFunctionName(),
                    rtype.getName(),
                    m.getName()
                );
                throw new IllegalArgumentException("Combiner: " + combiner.getFunctionName() + " can't be called for function: " + m.getName());
            }
        }
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
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
        throws ClassNotFoundException,
        InstantiationException,
        IllegalAccessException,
        InvocationTargetException {

        if (!(parent instanceof Column)) {
            return;
        }

        log.debug("Unmarshalling plan for column {}", ((Column) parent).getName());
        log.debug("combiner-glue: {}", combinerGlue);
        log.debug("Function count: {}", () -> CollectionUtils.size(functions));

        final Class<?> columnType = ((Column) parent).getType();
        initialize(columnType);
    }
}
