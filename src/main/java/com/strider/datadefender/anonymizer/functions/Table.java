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
package com.strider.datadefender.anonymizer.functions;

import com.strider.datadefender.functions.NamedParameter;
import com.strider.datadefender.requirement.registry.DatabaseAwareRequirementFunction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Helper anonymization using table data (shuffle records, etc...).
 *
 * @author Armenak Grigoryan
 */
@Log4j2
public class Table extends DatabaseAwareRequirementFunction {

    private static final Map<String, List<String>> stringLists = new HashMap<>();
    private static final Map<String, Iterator<String>> stringIters = new HashMap<>();
    private static final Map<String, Map<String, String>> predictableShuffle = new HashMap<>();

    /**
     * Returns the next shuffled item from the named collection.
     *
     * @param name
     * @return
     */
    private String getNextShuffledItemFor(final String name) {
        if (stringIters.containsKey(name)) {
            final Iterator<String> iter = stringIters.get(name);
            if (iter.hasNext()) {
                return iter.next();
            }
        }

        final List<String> list = stringLists.get(name);
        Collections.shuffle(list);

        final Iterator<String> iter = list.iterator();
        stringIters.put(name, iter);
        return iter.next();
    }

    /**
     * Sets up a map, mapping a list of values to a list of shuffled values.
     *
     * If the value is not mapped, the function guarantees returning the same
     * randomized value for a given column value - however it does not guarantee
     * that more than one column value do not have the same randomized value.
     *
     * @param params
     * @return
     */
    private String getPredictableShuffledValueFor(final String name, final String value) {
        if (!predictableShuffle.containsKey(name)) {
            final List<String> list = stringLists.get(name);
            final List<String> shuffled = new ArrayList<>(list);
            Collections.shuffle(shuffled);

            final Map<String, String> smap = new HashMap<>();
            final Iterator<String> lit = list.iterator();
            final Iterator<String> sit = shuffled.iterator();
            while (lit.hasNext()) {
                smap.put(lit.next(), sit.next());
            }
            predictableShuffle.put(name, smap);
        }

        final Map<String, String> map = predictableShuffle.get(name);
        if (!map.containsKey(value)) {
            final String[] vals = map.values().toArray(new String[map.size()]);
            final int index = (int) Math.abs((long) value.hashCode()) % vals.length;
            return vals[index];
        }
        return map.get(value);
    }

    /**
     * Creates a string list of values by querying the database.
     *
     * @param keyName
     * @param query
     * @throws java.sql.SQLException
     */
    protected void generateStringListFromDb(final String keyName, final String query) throws SQLException {
        if (!stringLists.containsKey(keyName + query.hashCode())) {
            log.info("*** reading from database column: " + keyName);
            final List<String> values = new ArrayList<>();

            log.debug("Query:" + query);
            Connection con = dbFactory.getConnection();
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    values.add(rs.getString(1));
                }
            }

            if (values.isEmpty()) {
                // TODO: throw a meaningful exception here
                log.error("!!! Database column " + keyName + " did not return any values");
            }
            stringLists.put(keyName + query.hashCode(), values);
        }
    }

    /**
     * Generates a randomized collection of column values and selects and
     * returns one.
     *
     * The function selects distinct, non-null and non-empty values to choose
     * from, then shuffles the collection of strings once before returning items
     * from it.  Once all strings have been returned, the collection is
     * re-shuffled and re-used.
     *
     * @param table the table name
     * @param column the column name
     * @param excludeEmpty set to true to exclude empty values
     * @return the next item
     * @throws SQLException
     */
    public String randomColumnValue(
        @NamedParameter("table") String table,
        @NamedParameter("column") String column,
        @NamedParameter("excludeEmpty") boolean excludeEmpty
    ) throws SQLException {
        
        final String keyName = table + "." + column;
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT DISTINCT %s FROM %s", column, table));
        if (excludeEmpty) {
            if (StringUtils.equalsIgnoreCase("oracle", dbFactory.getVendorName())) {
                sb.append(String.format(" WHERE %s IS NOT NULL", column, column));
            } else {
                sb.append(String.format(" WHERE %s IS NOT NULL AND %s <> ''", column, column));
            }
        }
        generateStringListFromDb(keyName, sb.toString());
        return getNextShuffledItemFor(keyName + sb.toString().hashCode());
    }

    /**
     * Returns a 'predictable' shuffled value based on the passed value which is
     * guaranteed to return the same random value for the same column value.
     *
     * Note that more than one column value may result in having the same
     * shuffled value.
     *
     * @param table
     * @param column
     * @param value
     * @param excludeEmpty
     * @return
     * @throws SQLException
     */
    public String mappedColumnShuffle(
        @NamedParameter("table") String table,
        @NamedParameter("column") String column,
        @NamedParameter("value") String value,
        @NamedParameter("excludeEmpty") boolean excludeEmpty
    ) throws SQLException {
        
        final String keyName = table + "." + column;
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT DISTINCT %s FROM %s", column, table));
        if (excludeEmpty) {
            if (StringUtils.equalsIgnoreCase("oracle", dbFactory.getVendorName())) {
                sb.append(String.format(" WHERE %s IS NOT NULL", column, column));
            } else {
                sb.append(String.format(" WHERE %s IS NOT NULL AND %s <> ''", column, column));
            }
        }
        generateStringListFromDb(keyName, sb.toString());
        return getPredictableShuffledValueFor(keyName + sb.toString().hashCode(), value);
    }
}
