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
package com.strider.dataanonymizer.utils;

import com.strider.dataanonymizer.requirement.Column;
import com.strider.dataanonymizer.requirement.Parameter;
import com.strider.dataanonymizer.requirement.Requirement;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test to test requirement related utility methods
 *
 * @author Matthew Eaton
 */
public class RequirementUtilsTest extends TestCase {
    private Requirement requirement = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        requirement = RequirementUtils.load(
                getClass().getResource("/Requirement.xml").getFile());

    }

    /**
     * Test loaded requirements from file
     * @author Matthew Eaton
     * @throws Exception
     */
    public void testLoad() throws Exception {

        assertNotNull(requirement);
        assertEquals("Test Client", requirement.getClient());
        assertEquals("1.0", requirement.getVersion());
        assertEquals(1, requirement.getTables().size());
        assertEquals("test_table", requirement.getTables().get(0).getName());

        int columnNo = 0;
        for (Column column : requirement.getTables().get(0).getColumns()) {
            assertEquals(("column" + ++columnNo), column.getName());
        }

        assertEquals(5, columnNo);
    }

    /**
     * Test getting parameters with name "file"
     * @author Matthew Eaton
     * @throws Exception
     */
    public void testGetFileParameter() throws Exception {

        assertNotNull(requirement);

        int columnNo = 0;
        for (Column column : requirement.getTables().get(0).getColumns()) {
            if ("column1".equals(column.getName()) || "column2".equals(column.getName())) {
                Parameter parameter = RequirementUtils.getFileParameter(column.getParameters());

                assertNotNull(parameter);
                assertEquals(RequirementUtils.PARAM_NAME_FILE, parameter.getName());

            } else {
                Parameter parameter = RequirementUtils.getFileParameter(column.getParameters());

                assertNull(parameter);

            }

            assertEquals(("column" + ++columnNo), column.getName());
        }


    }
}