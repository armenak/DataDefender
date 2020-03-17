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
package com.strider.datadefender.requirement.functions;

import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.requirement.Requirement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

/**
 * Very basic registry for classes that need to be instantiated for use by
 * requirement functions.
 *
 * @author Zaahid Bateson <zaahid.bateson@ubc.ca>
 */
@RequiredArgsConstructor
public class RequirementFunctionClassRegistry {

    private static RequirementFunctionClassRegistry instance = new RequirementFunctionClassRegistry();

    private Map<Class<?>, RequirementFunctionClass> singletons = new HashMap<>();

    public RequirementFunctionClass getFunctionsSingleton(Class<?> cls) {
        return singletons.get(cls);
    }

    public void register(Requirement requirements) throws
        NoSuchMethodException,
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {
        List<Method> fns = requirements.getTables().stream()
            .flatMap((t) -> t.getColumns().stream())
            .map((c) -> c.getFunction())
            .collect(Collectors.toList());
        for (Method m : fns) {
            Class<?> cls = m.getDeclaringClass();
            if (RequirementFunctionClass.class.isAssignableFrom(cls)) {
                singletons.put(cls, (RequirementFunctionClass) cls.getDeclaredConstructor().newInstance());
            }
        }
    }

    public void initialize(IDbFactory factory) {
        for (Object o : singletons.values()) {
            if (o instanceof DatabaseAwareRequirementFunctionClass) {
                ((DatabaseAwareRequirementFunctionClass) o).initialize(factory);
            }
        }
    }

    public static RequirementFunctionClassRegistry singleton() {
        return instance;
    }
}
