/*
 *
 * Copyright 2015, Armenak Grigoryan, and individual contributors as indicated
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
 *
 */



package com.strider.datadefender.utils;

/**
 * @author Akira Matsuo
 *
 * @param <T>
 * Much like a java.util.Supplier but throws an exception.
 * @param <E>
 */
@FunctionalInterface
public interface ISupplierWithException<T, E extends Exception> {
    T get() throws E;
}


//~ Formatted by Jindent --- http://www.jindent.com
