/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strider.datadefender.database.metadata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Public interface to define the contract for all database-specific
 * metadata classes.
 * 
 * @author armenak
 */
public interface IMetaData {
    List<MatchMetaData> getMetaData();
    List<MatchMetaData> getMetaData(String columnType);
    List<MatchMetaData> getMetaDataForRs(ResultSet rs) throws SQLException;
}
