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

package com.strider.dataanonymizer;

import java.util.List;

import com.strider.dataanonymizer.database.metadata.MatchMetaData;
import com.strider.dataanonymizer.requirement.Requirement;
import com.strider.dataanonymizer.utils.RequirementUtils;

/**
 * Holds common logic for Discoverers.
 * @author Akira Matsuo
 */
public abstract class Discoverer implements IDiscoverer {
    
    protected List<MatchMetaData> matches;

    public void createRequirement(String fileName) throws AnonymizerException {
        if (matches == null || matches.isEmpty()) {
            throw new AnonymizerException("No matches to create requirement from!");
        }
        Requirement requirement = RequirementUtils.create(matches);
        RequirementUtils.write(requirement, fileName);
    }
}
