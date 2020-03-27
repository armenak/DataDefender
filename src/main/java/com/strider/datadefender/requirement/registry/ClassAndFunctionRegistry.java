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
package com.strider.datadefender.requirement.registry;

import com.mchange.v1.lang.ClassUtils;
import com.strider.datadefender.database.IDbFactory;
import com.strider.datadefender.requirement.Requirement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Very basic registry for classes that need to be instantiated for use by
 * requirement functions.
 *
 * @author Zaahid Bateson <zaahid.bateson@ubc.ca>
 */
@RequiredArgsConstructor
@Log4j2
public class ClassAndFunctionRegistry {

    private static ClassAndFunctionRegistry instance = new ClassAndFunctionRegistry();

    private Map<Class<?>, RequirementFunction> singletons = new HashMap<>();
    private Set<String> autoResolvePackages = new LinkedHashSet<>();

    public RequirementFunction getFunctionsSingleton(Class<?> cls) {
        return singletons.get(cls);
    }

    /**
     * Registers the passed package name as auto-resolvable, so classes under it
     * don't need to be fully qualified.
     *
     * @param name
     */
    public void registerAutoResolvePackage(String name) {
        log.debug("Registering autoresolve package {}", name);
        autoResolvePackages.add(name);
    }

    /**
     * Returns a Class object for the passed name, using autoresolve packages if
     * the name of the passed class starts with an uppercase letter, and does
     * not contain any "." characters.
     * 
     * This means inner classes can't be autoresolved.
     *
     * @param className
     * @return
     */
    public Class<?> getClassForName(String className) throws ClassNotFoundException {
        if (!className.contains(".") && Character.isUpperCase(className.charAt(0))) {
            for (String pkg : autoResolvePackages) {
                try {
                    Class<?> c = ClassUtils.forName(pkg + "." + className);
                    if (Modifier.isPublic(c.getModifiers())) {
                        log.debug("{} class autoresolved to {}", () -> className, () -> pkg + "." + className);
                        return c;
                    }
                } catch (ClassNotFoundException e) {
                    log.debug("Class with name {} not found while autoresolving", () -> pkg + "." + className);
                }
            }
        }
        return ClassUtils.forName(className);
    }

    public void registerFunctions(Requirement requirements)
        throws NoSuchMethodException,
        InstantiationException,
        IllegalAccessException,
        IllegalArgumentException,
        InvocationTargetException {

        log.debug("Number of tables: {}", () -> CollectionUtils.emptyIfNull(requirements.getTables()).size());
        List<Method> fns = Stream.concat(
            requirements.getTables().stream()
                .flatMap((t) -> t.getColumns().stream())
                .flatMap((c) -> c.getResolvedPlan().getFunctions().stream()),
            requirements.getTables().stream()
                .flatMap((t) -> t.getColumns().stream())
                .map((c) -> c.getResolvedPlan().getCombiner())
        )
            .filter((fn) -> fn != null)     // combiner could be null
            .map((fn) -> fn.getFunction())
            .collect(Collectors.toList());

        log.debug("Found {} classes to register with functions for anonymization", fns.size());
        for (Method m : fns) {
            Class<?> cls = m.getDeclaringClass();
            log.debug(
                "Class: {}, is a RequirementFunctionClass: {}",
                () -> cls.getName(),
                () -> RequirementFunction.class.isAssignableFrom(cls)
            );
            if (RequirementFunction.class.isAssignableFrom(cls)) {
                log.debug("Registering RequirementFunctionClass: {}", cls);
                singletons.put(cls, (RequirementFunction) cls.getDeclaredConstructor().newInstance());
            }
        }
    }

    public void initialize(IDbFactory factory) {
        for (Object o : singletons.values()) {
            if (o instanceof DatabaseAwareRequirementFunction) {
                log.debug("Initializing DatabaseAwareRequirementFunctionClass for: {}", o.getClass());
                ((DatabaseAwareRequirementFunction) o).initialize(factory);
            }
        }
    }

    public static ClassAndFunctionRegistry singleton() {
        return instance;
    }
}
