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
package com.strider.datadefender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Option;

import lombok.Getter;

/**
 * Database configuration options for picocli.
 * 
 * @author Zaahid Bateson
 */
@Getter
public class DbConfig {

    private String vendor;

    @Option(names = { "--schema" }, description = "The schema to connect to", required = false)
    private String schema;

    private String url;

    @Option(
        names = { "--vendor" },
        description = "Database vendor, available options are: h2, mysql, mariadb, postgresql, sqlserver, oracle. "
            + "If not specified, vendor will attempt to be extracted from the datasource url for a jdbc scheme.",
        required = false
    )
    public void setVendor(String vendor) {
        if (!StringUtils.equalsAny(vendor, "h2", "mysql", "mariadb", "postgresql", "mssql", "sqlserver", "oracle")) {
            throw new IllegalArgumentException(
                "Invalid value for option '--vendor': Valid options are: "
                + "h2, mysql, postgresql, mssql and oracle."
            );
        }
        if (vendor.equals("mariadb")) {
            this.vendor = "mysql";
        } else if (vendor.equals("mssql")) {
            // old versions of DataDefender used the name 'mssql'
            this.vendor = "sqlserver";
        } else {
            this.vendor = vendor;
        }
    }

    @Option(names = { "--url" }, description = "The datasource URL", required = true)
    public void setUrl(String url) {
        Pattern p = Pattern.compile("\\s*jdbc:([^:]+):.*");
        if (vendor == null) {
            Matcher m = p.matcher(url);
            if (m.find()) {
                setVendor(m.group(1));
            }
        }
        this.url = url;
    }
}
