/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.dataanonymizer.database.metadata;

import java.util.List;
import java.util.Properties;

/**
 * Public interface to define the contract for all database-specific
 * metadata classes.
 * 
 * @author armenak
 */
public interface IMetaData {
    public List<ColumnMetaData> getMetaData();
}
