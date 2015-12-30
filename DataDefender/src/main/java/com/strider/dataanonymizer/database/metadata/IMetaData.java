/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.dataanonymizer.database.metadata;

import java.util.List;

/**
 * Public interface to define the contract for all database-specific
 * metadata classes.
 * 
 * @author armenak
 */
public interface IMetaData {
    public List<MatchMetaData> getMetaData();
    public List<MatchMetaData> getMetaData(String columnType);
}
