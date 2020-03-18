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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.ClassUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Provides 'convert' and 'isConvertible' static methods for detection/
 * conversion of types for Function and Column elements.
 *
 * @author Zaahid Bateson
 */
@Log4j2
public class TypeConverter {

    private static Map<Integer, Integer> cachedConversionWeight = new HashMap<>();

    private TypeConverter() {
    }

    /**
     * Returns true if an object of type "from" can be converted to type "to" by
     * the 'convert' method.
     *
     * @param from
     * @param to
     * @return
     */
    public static boolean isConvertible(Class<?> from, Class<?> to) {
        if (ClassUtils.isAssignable(from, to) || String.class.equals(to)) {
            return true;
        }
        return (getConvertibleConstructor(from, to) != null);
    }

    /**
     * Converts the passed value to the passed type if possible.
     *
     * Conversion is performed in the following order:
     *  - Returned as-is if the value is of the same type or a sub-class of the
     *    type.
     *  - If type is java.lang.String, call toString on value and return it.
     *  - If value is a primitive, primitive wrapper, or String, and type
     *    represents one of those as well, an attempt is made to convert from/to
     *    as needed.
     *  - Otherwise, look for a constructor in the passed type that accepts a
     *    single argument of:
     *    o value itself (as its type or an interface/superclass)
     *    o A String argument, in which case toString is called on value
     *    o If value is primitive, the primitive type or it's wrapper
     *    o If value is a String, a primitive or wrapper argument
     *
     * @param value
     * @param type
     * @return
     */
    public static Object convert(Object value, Class<?> type)
        throws InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
        if (ClassUtils.isAssignable(value.getClass(), type)) {
            return value;
        } else if (String.class.equals(type)) {
            return value.toString();
        } else if (ClassUtils.isPrimitiveOrWrapper(type) && value instanceof String) {
            return ConvertUtils.convert(value, type);
        }
        Constructor<?> constr = getConvertibleConstructor(value.getClass(), type);
        Class<?> pt = (constr != null && constr.getParameterCount() > 0) ? constr.getParameterTypes()[0] : null;
        if (!ClassUtils.isAssignable(value.getClass(), pt)) {
            if (pt != null && ClassUtils.isAssignable(String.class, pt)) {
                return constr.newInstance(value.toString());
            } else if (pt != null && ClassUtils.isPrimitiveOrWrapper(pt) && value instanceof String) {
                return constr.newInstance(ConvertUtils.convert(value, pt));
            }
            // try anyway...
        }

        return constr.newInstance(value);
    }

    private static Constructor<?> getConstructorInOrder(Class<?> in, List<Class<?>> cls) {
        List<Constructor<?>> constructors = Arrays.stream(in.getConstructors()).filter((c) -> c.getParameterCount() == 1).collect(Collectors.toList());
        for (Class<?> p : cls) {
            for (Constructor<?> c : constructors) {
                if (ClassUtils.isAssignable(p, c.getParameterTypes()[0])) {
                    return c;
                }
            }
        }
        return null;
    }

    private static List<Class<?>> buildOrderedListForType(Class<?> from) {
        List<Class<?>> list = new ArrayList<>();
        list.add(from);
        if (ClassUtils.isPrimitiveOrWrapper(from)) {
            list.add(from.isPrimitive() ? ClassUtils.primitiveToWrapper(from) : ClassUtils.wrapperToPrimitive(from));
        }
        if (String.class.equals(from)) {
            list.addAll(List.of(
                double.class, Double.class, float.class, Float.class, long.class, Long.class,
                int.class, Integer.class, short.class, Short.class, byte.class, Byte.class,
                boolean.class, Boolean.class, char.class, Character.class
            ));
        } else {
            list.add(String.class);
        }
        return list;
    }

    private static Constructor<?> getConvertibleConstructor(Class<?> from, Class<?> to) {
        return getConstructorInOrder(
            to,
            buildOrderedListForType(from)
        );
    }
}
