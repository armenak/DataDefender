/*
 * Copyright 2020, Armenak Grigoryan, and individual contributors as indicated
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
package com.strider.datadefender.functions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares a parameter's name for reflection.
 *
 * Provides an alternative to compiling with -parameters so a method's
 * parameters can be named and referenced by name.
 *
 * @author Zaahid Bateson
 */
@Documented
@Target({ ElementType.PARAMETER })
public @interface NamedParameter {
    /**
     * The name of the parameter that should be used to reference it in
     * external configuration.
     * @return
     */
    public String value();
}
