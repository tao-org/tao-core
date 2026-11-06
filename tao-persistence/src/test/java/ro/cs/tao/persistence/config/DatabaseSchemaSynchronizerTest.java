package ro.cs.tao.persistence.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseSchemaSynchronizerTest {

    @Test
    public void normalizeTypeTreatsPostgreSqlIntegerAliasesAsEquivalent() {
        assertEquals("smallint", DatabaseSchemaSynchronizer.normalizeType("smallint"));
        assertEquals("smallint", DatabaseSchemaSynchronizer.normalizeType("int2"));
        assertEquals("integer", DatabaseSchemaSynchronizer.normalizeType("integer"));
        assertEquals("integer", DatabaseSchemaSynchronizer.normalizeType("int4"));
        assertEquals("bigint", DatabaseSchemaSynchronizer.normalizeType("bigint"));
        assertEquals("bigint", DatabaseSchemaSynchronizer.normalizeType("int8"));
    }

    @Test
    public void normalizeTypeTreatsCommonPostgreSqlAliasesAsEquivalent() {
        assertEquals("varchar(255)", DatabaseSchemaSynchronizer.normalizeType("character varying(255)"));
        assertEquals("char(1)", DatabaseSchemaSynchronizer.normalizeType("bpchar(1)"));
        assertEquals("boolean", DatabaseSchemaSynchronizer.normalizeType("bool"));
        assertEquals("real", DatabaseSchemaSynchronizer.normalizeType("float4"));
        assertEquals("double precision", DatabaseSchemaSynchronizer.normalizeType("float8"));
        assertEquals("numeric(10,2)", DatabaseSchemaSynchronizer.normalizeType("decimal(10, 2)"));
        assertEquals("timestamp", DatabaseSchemaSynchronizer.normalizeType("timestamp without time zone"));
    }
}
