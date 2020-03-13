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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import picocli.CommandLine.Option;

import lombok.Getter;

/**
 * Database configuration options for picocli.
 * 
 * @author Zaahid Bateson
 */
@Getter
public class DbConfig {

    public enum Vendor {
        H2, MYSQL, POSTGRESQL, SQLSERVER, ORACLE;
    }

    private static final Map<String, Vendor> VENDOR_MAP = Map.of(
        "h2", Vendor.H2,
        "mysql", Vendor.MYSQL,
        "mariadb", Vendor.MYSQL,
        "postgresql", Vendor.POSTGRESQL,
        "sqlserver", Vendor.SQLSERVER,
        "mssql", Vendor.SQLSERVER,
        "oracle", Vendor.ORACLE
    );

    private Vendor vendor;

    @Option(names = { "-u", "--user" }, description = "The username to connect with", required = false)
    private String username;
    
    @Option(names = { "-p", "--password" }, description = "The password to connect with", required = false, arity = "0..1", interactive = true)
    private String password;

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
        String c = vendor.trim().toLowerCase();
        if (!VENDOR_MAP.containsKey(c)) {
            throw new IllegalArgumentException(
                "Invalid value for option '--vendor': Valid options are: "
                + "h2, mysql, mariadb, postgresql, sqlserver and oracle."
            );
        }
        this.vendor = VENDOR_MAP.get(c);
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
