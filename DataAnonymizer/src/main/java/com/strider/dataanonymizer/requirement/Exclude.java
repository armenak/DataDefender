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

package com.strider.dataanonymizer.requirement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * JAXB class definition for <Exclude> tags defining exclusion rules.
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
public class Exclude {
    
    /**
     * The column name in the database
     */
    @XmlAttribute(name="Name")
    private String name;
    
    /**
     * The excluded value if the value should match with an SQL = comparison in
     * the WHERE clause
     */
    @XmlAttribute(name="Equals")
    private String equals;
    
    /**
     * The excluded value if the value should match with an SQL LIKE comparison
     * in the WHERE clause
     */
    @XmlAttribute(name="Like")
    private String like;
    
    /**
     * The excluded value includes nulls.
     */
    @XmlAttribute(name="Null")
    private String excludeNull;
    
    /**
     * Returns the column name represented by this exclusion
     * 
     * @return String
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the excluded (identical) value for the column
     * 
     * @return String
     */
    public String getEqualsValue() {
        return this.equals;
    }
    
    /**
     * Returns the excluded (LIKE) value for the column
     * 
     * @return String
     */
    public String getLikeValue() {
        return this.like;
    }
    
    /**
     * Returns true if the Null attribute is set
     * 
     * @return boolean
     */
    public boolean isExcludeNulls() {
        return (this.excludeNull != null && this.excludeNull.equals("true"));
    }
    
    /**
     * Sets both the equals value to an empty string, and excludeNulls to "true"
     * for the "IgnoreEmpty" shortcut.
     */
    public void setIgnoreEmpty() {
        equals = "";
        excludeNull = "true";
    }
}
