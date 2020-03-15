/*
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
 */
package com.strider.datadefender.requirement.file;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utility class to check compatibility of versions.
 *
 * Used to compare versions in the XML file against the app version.
 *
 * @author Zaahid Bateson
 */
public class VersionUtil {

    /**
     * Returns true if the major version of the two strings is the same, and if
     * the minor version of fileVersion is less than or equal to the minor
     * version of the app version.
     *
     * @param appVersion
     * @param fileVersion
     * @return
     */
    public static boolean isCompatible(String appVersion, String fileVersion) {
        if (StringUtils.equals(appVersion, fileVersion)) {
            return true;
        }
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)*");
        Matcher app = p.matcher(appVersion);
        Matcher file = p.matcher(fileVersion);
        if (!app.matches()) {
            throw new IllegalArgumentException(
                "Application compiled with bad version number: " + appVersion
            );
        } else if (!file.matches()) {
            throw new IllegalArgumentException(
                "File version number not in form x.x.x: " + fileVersion
            );
        }
        if (NumberUtils.toInt(app.group(1)) != NumberUtils.toInt(file.group(1))) {
            return false;
        }

        // ensure "1.02" doesn't compare equally to "1.2"
        return (NumberUtils.toDouble("0." + app.group(2)) >= NumberUtils.toDouble("0." + file.group(2)));
    }
}
