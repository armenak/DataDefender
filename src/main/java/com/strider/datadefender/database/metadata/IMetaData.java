package com.strider.datadefender.database.metadata;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Set;

/**
 * Public interface to define the contract for all database-specific
 * metadata classes.
 *
 * @author armenak
 */
public interface IMetaData {
    List<MatchMetaData> getMetaData(String vendor);

    // List<MatchMetaData> getMetaData(final String columnType);
    List<MatchMetaData> getMetaDataForRs(final ResultSet rs) throws SQLException;
}