/*
 *
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
 *
 */



package com.strider.datadefender.database;

import com.strider.datadefender.DataDefenderException;

/**
 * Package level exception. Extends application level exception.
 * @author Armenak Grigoryan
 */
public class DatabaseAnonymizerException extends DataDefenderException {
    private static final long serialVersionUID = 1L;

    public DatabaseAnonymizerException(final String msg) {
        super(msg);
    }

    public DatabaseAnonymizerException(final String msg, final Throwable t) {
        super(msg, t);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
