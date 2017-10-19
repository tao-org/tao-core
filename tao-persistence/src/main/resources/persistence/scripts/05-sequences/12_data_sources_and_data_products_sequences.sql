----- table: tao.data_format ----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.data_format_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.data_format_id_seq
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.data_format ALTER COLUMN id SET DEFAULT nextval('tao.data_format_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.data_format_id_seq OWNED BY tao.data_format.id;


----- table: tao.pixel_type -----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.pixel_type_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.pixel_type_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.pixel_type ALTER COLUMN id SET DEFAULT nextval('tao.pixel_type_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.pixel_type_id_seq OWNED BY tao.pixel_type.id;


----- table: tao.sensor_type ----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.sensor_type_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.sensor_type_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.sensor_type ALTER COLUMN id SET DEFAULT nextval('tao.sensor_type_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.sensor_type_id_seq OWNED BY tao.sensor_type.id;


----- table: tao.orbit_direction ------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.orbit_direction_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.orbit_direction_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.orbit_direction ALTER COLUMN id SET DEFAULT nextval('tao.orbit_direction_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.orbit_direction_id_seq OWNED BY tao.orbit_direction.id;


----- table: tao.polarisation_mode ----------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.polarisation_mode_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.polarisation_mode_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.polarisation_mode ALTER COLUMN id SET DEFAULT nextval('tao.polarisation_mode_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.polarisation_mode_id_seq OWNED BY tao.polarisation_mode.id;


----- table: tao.polarisation_channel -------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.polarisation_channel_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.polarisation_channel_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.polarisation_channel ALTER COLUMN id SET DEFAULT nextval('tao.polarisation_channel_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.polarisation_channel_id_seq OWNED BY tao.polarisation_channel.id;


----- table: tao.data_source_type -----------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.data_source_type_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.data_source_type_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.data_source_type ALTER COLUMN id SET DEFAULT nextval('tao.data_source_type_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.data_source_type_id_seq OWNED BY tao.data_source_type.id;


----- table: tao.data_source ----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.data_source_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.data_source_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.data_source ALTER COLUMN id SET DEFAULT nextval('tao.data_source_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.data_source_id_seq OWNED BY tao.data_source.id;


----- table: tao.data_type ------------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.data_type_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.data_type_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.data_type ALTER COLUMN id SET DEFAULT nextval('tao.data_type_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.data_type_id_seq OWNED BY tao.data_type.id;


----- table: tao.query_parameter ------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.query_parameter_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.query_parameter_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.query_parameter ALTER COLUMN id SET DEFAULT nextval('tao.query_parameter_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.query_parameter_id_seq OWNED BY tao.query_parameter.id;


----- table: tao.data_query -----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.data_query_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.data_query_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.data_query ALTER COLUMN id SET DEFAULT nextval('tao.data_query_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.data_query_id_seq OWNED BY tao.data_query.id;

