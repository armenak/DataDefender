package com.strider.datadefender.database.metadata;

import com.strider.datadefender.DbConfig;
import com.strider.datadefender.database.metadata.TableMetaData.ColumnMetaData;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Akira Matsuo
 *
 * Tests the default implementation of MetaData that's used for Oracle and MSSQL.
 *
 */
@ExtendWith(MockitoExtension.class)
public class MetaDataTest {

    @Mock
    Connection mockConnection;
    @Mock
    DbConfig mockConfig;
    @Mock
    SqlTypeToClass mockSqlTypeMap;
    @Mock
    ResultSet mockTableRs;
    @Mock
    ResultSet mockColumnRs;
    @Mock
    DatabaseMetaData mockDbMetaData;

    @BeforeEach
    public void setUp() throws Exception {

        when(mockConfig.getSchema()).thenReturn("such-schema");
        when(mockConnection.getMetaData()).thenReturn(mockDbMetaData);

        when(mockDbMetaData.getTables(any(), any(), any(), any())).thenReturn(mockTableRs);
        when(mockTableRs.next()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(mockTableRs.getString("TABLE_NAME")).thenReturn("such-table");

        when(mockDbMetaData.getColumns(any(), any(), any(), any())).thenReturn(mockColumnRs);
        when(mockColumnRs.next()).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(mockColumnRs.getString("COLUMN_NAME")).thenReturn("such-column");
        when(mockColumnRs.getInt("COLUMN_SIZE")).thenReturn(1337);
        when(mockColumnRs.getInt("DATA_TYPE")).thenReturn(Types.CHAR);
        when(mockSqlTypeMap.getTypeFrom(Types.CHAR)).thenReturn(String.class);
    }

    @Test
    public void testGetMetaData() throws Exception {

        MetaData test = new MetaData(mockConfig, mockConnection, mockSqlTypeMap);
        List<TableMetaData> ret = test.getMetaData();

        verify(mockDbMetaData, times(1)).getTables(isNull(), eq("such-schema"), isNull(), eq(new String[] { "TABLE" }));
        verify(mockTableRs, times(2)).next();
        verify(mockDbMetaData, times(1)).getColumns(isNull(), eq("such-schema"), eq("such-table"), isNull());
        verify(mockColumnRs, times(2)).next();
        verify(mockColumnRs).getString(eq("COLUMN_NAME"));
        verify(mockColumnRs).getInt(eq("COLUMN_SIZE"));
        verify(mockColumnRs).getInt(eq("DATA_TYPE"));

        assertNotNull(ret);
        assertEquals(1, ret.size());
        assertNotNull(ret.get(0));
        assertEquals("such-schema", ret.get(0).getSchemaName());
        assertEquals("such-table", ret.get(0).getTableName());

        assertNotNull(ret.get(0).getColumns());
        assertEquals(1, ret.get(0).getColumns().size());

        ColumnMetaData col = ret.get(0).getColumn(0);
        assertNotNull(col);
        assertEquals("such-column", col.getColumnName());
        assertSame(col, ret.get(0).getColumn("such-column"));
        assertEquals(1337, col.getColumnSize());
        assertEquals(String.class, col.getColumnType());
    }
}
