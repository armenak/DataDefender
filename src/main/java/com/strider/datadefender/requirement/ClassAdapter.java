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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Zaahid Bateson
 */
public class ClassAdapter extends XmlAdapter<String, Class<?>> {

    @Override
    public Class<?> unmarshal(String value) throws Exception {
        String t = StringUtils.trimToEmpty(value);
        if (StringUtils.isBlank(t)) {
            t = "java.lang.String";
        } else if (!t.contains(".") && Character.isUpperCase(t.charAt(0))) {
            t = "java.lang." + t;
        }
        if (t.endsWith("[]")) {
            t = t.substring(0, t.length() - 2);
        }
        return ClassUtils.getClass(t);
    }

    @Override
    public String marshal(Class<?> bt) throws Exception {
        return ClassUtils.getCanonicalName(bt);
    }
}
