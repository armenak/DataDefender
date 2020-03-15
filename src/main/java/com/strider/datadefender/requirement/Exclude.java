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

import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JAXB class definition for &lt;Exclude&gt; tags defining exclusion rules.
 *
 * The exclude tag has a Name attribute, and either an Equals attribute, a Like
 * attribute (corresponding to an SQL LIKE comparison), or a Null attribute.
 * The Name attribute defines the column's name in the table, and Equals,
 * Like, and Null attributes define the column's excluded value.
 *
 * The Name attribute is optional if the exclude tag is part of a Column tag's
 * exclusion list (the name is then the Column's name).  The Name attribute can
 * still be included to indicate it should be excluded on the basis of the value
 * of another column - however the other column must either be declared as a key
 * or as a Column elsewhere in the table definition.  This restriction does not
 * apply to table exclusions.  Any column can be used for a table exclusion.
 *
 * Excluding empty values is possible by setting the Equals attribute to a blank
 * value.  The Like attribute requires a value.
 *
 * Exclude nulls with the optional Null attribute (takes a boolean value).
 *
 * Inclusions can be defined with the NotLike and NotEquals attribute.  In a
 * list of &lt;Exclusions&gt; and associated &lt;Exclude&gt; tags, a column's
 * value must match at least one NotLike or NotEquals definition if any are
 * defined.
 *
 * Note that column exclusions are not actual SQL exclusions - so some
 * differences may apply, for instance in SQL an = comparison may be case
 * insensitive depending on the character encoding used on the table, and the
 * type of database being queried.
 *
 * @see Table::getExclusions
 * @see Column::getExclusions
 * @author Zaahid Bateson
 */
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@Data
public class Exclude {

    /**
     * Sets both the equals value to an empty string, and excludeNulls to "true"
     * for the "IgnoreEmpty" shortcut.
     */
    Exclude(boolean ignoreEmpty) {
        if (ignoreEmpty) {
            equals = "";
            excludeNull = true;
        }
    }

    /**
     * The column name in the database
     */
    @XmlAttribute(name = "Name")
    private String name;

    /**
     * The excluded value if the value should match with an SQL = comparison in
     * the WHERE clause
     */
    @XmlAttribute(name = "Equals")
    private String equals;

    /**
     * The excluded value if the value should match with an SQL LIKE comparison
     * in the WHERE clause
     */
    @XmlAttribute(name = "Like")
    private String like;

    /**
     * The excluded value if the value should match with an SQL != comparison in
     * the WHERE clause.
     *
     * NotEquals values represent "inclusions", where one NotEquals value must
     * be matched in a series of listed <Exclusions> for the column's value to
     * be anonymized.
     */
    @XmlAttribute(name = "NotEquals")
    private String notEquals;

    /**
     * The excluded value if the value should match with an SQL NOT LIKE
     * comparison in the WHERE clause.
     *
     * NotLike values represent "inclusions", where one NotEquals value must be
     * matched in a series of listed <Exclusions> for the column's value to be
     * anonymized.
     */
    @XmlAttribute(name = "NotLike")
    private String notLike;

    /**
     * The excluded value includes nulls.
     */
    @XmlAttribute(name = "Null")
    private boolean excludeNull;

    /**
     * Uses an "IN ()" clause to exclude a large range of values.
     */
    @XmlAttribute(name = "In")
    private String excludeIn;

    /**
     * Uses an "NOT IN ()" clause to exclude a large range of values.
     */
    @XmlAttribute(name = "NotIn")
    private String excludeNotIn;

    /**
     * Separator String for In attribute, defaults to ",".
     */
    @XmlAttribute(name = "InSeparator")
    private String inSeparator = ",";

    /**
     * Generates a List for "IN" exclusions using the set separator.
     * 
     * @return
     */
    public List<String> getExcludeInList() {
        if (StringUtils.isBlank(excludeIn)) {
            return List.of();
        }
        return Arrays.asList(
            StringUtils.splitByWholeSeparator(excludeIn, inSeparator)
        );
    }

    /**
     * Generates a List for "NOT IN" exclusions using the set separator.
     *
     * @return
     */
    public List<String> getExcludeNotInList() {
        if (StringUtils.isBlank(excludeNotIn)) {
            return List.of();
        }
        return Arrays.asList(
            StringUtils.splitByWholeSeparator(excludeNotIn, inSeparator)
        );
    }
}
