-------------------------------------------------------------------------------
-- table: product.data_format
DROP TABLE IF EXISTS product.data_format CASCADE;
CREATE TABLE product.data_format
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE product.data_format ADD CONSTRAINT PK_data_format
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.data_format_id_seq CASCADE;
CREATE SEQUENCE product.data_format_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.data_format ALTER COLUMN id SET DEFAULT nextval('product.data_format_id_seq');
ALTER SEQUENCE product.data_format_id_seq OWNED BY product.data_format.id;

-------------------------------------------------------------------------------
-- table: product.pixel_type
DROP TABLE IF EXISTS product.pixel_type CASCADE;
CREATE TABLE product.pixel_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE product.pixel_type ADD CONSTRAINT PK_pixel_type
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.pixel_type_id_seq CASCADE;
CREATE SEQUENCE product.pixel_type_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.pixel_type ALTER COLUMN id SET DEFAULT nextval('product.pixel_type_id_seq');
ALTER SEQUENCE product.pixel_type_id_seq OWNED BY product.pixel_type.id;

-------------------------------------------------------------------------------
-- table: product.sensor_type
DROP TABLE IF EXISTS product.sensor_type CASCADE;
CREATE TABLE product.sensor_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE product.sensor_type ADD CONSTRAINT PK_sensor_type
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.sensor_type_id_seq CASCADE;
CREATE SEQUENCE product.sensor_type_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.sensor_type ALTER COLUMN id SET DEFAULT nextval('product.sensor_type_id_seq');
ALTER SEQUENCE product.sensor_type_id_seq OWNED BY product.sensor_type.id;

-------------------------------------------------------------------------------
-- table: product.orbit_direction
DROP TABLE IF EXISTS product.orbit_direction CASCADE;
CREATE TABLE product.orbit_direction
(
	id integer NOT NULL,
	direction varchar(50) NOT NULL
);
ALTER TABLE product.orbit_direction ADD CONSTRAINT PK_orbit_direction
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.orbit_direction_id_seq CASCADE;
CREATE SEQUENCE product.orbit_direction_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.orbit_direction ALTER COLUMN id SET DEFAULT nextval('product.orbit_direction_id_seq');
ALTER SEQUENCE product.orbit_direction_id_seq OWNED BY product.orbit_direction.id;

-------------------------------------------------------------------------------
-- table: product.polarisation_mode
DROP TABLE IF EXISTS product.polarisation_mode CASCADE;
CREATE TABLE product.polarisation_mode
(
	id integer NOT NULL,
	mode varchar(50) NOT NULL
);
ALTER TABLE product.polarisation_mode ADD CONSTRAINT PK_polarisation_mode
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.polarisation_mode_id_seq CASCADE;
CREATE SEQUENCE product.polarisation_mode_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.polarisation_mode ALTER COLUMN id SET DEFAULT nextval('product.polarisation_mode_id_seq');
ALTER SEQUENCE product.polarisation_mode_id_seq OWNED BY product.polarisation_mode.id;

-------------------------------------------------------------------------------
-- table: product.polarisation_channel
DROP TABLE IF EXISTS product.polarisation_channel CASCADE;
CREATE TABLE product.polarisation_channel
(
	id integer NOT NULL,
	channel varchar(50) NOT NULL
);
ALTER TABLE product.polarisation_channel ADD CONSTRAINT PK_polarisation_channel
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.polarisation_channel_id_seq CASCADE;
CREATE SEQUENCE product.polarisation_channel_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.polarisation_channel ALTER COLUMN id SET DEFAULT nextval('product.polarisation_channel_id_seq');
ALTER SEQUENCE product.polarisation_channel_id_seq OWNED BY product.polarisation_channel.id;

-------------------------------------------------------------------------------
-- table: product.product_status
DROP TABLE IF EXISTS product.product_status CASCADE;
CREATE TABLE product.product_status
(
	id integer NOT NULL,
	status varchar(50) NOT NULL
);
ALTER TABLE product.product_status ADD CONSTRAINT PK_product_status
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.product_status_id_seq CASCADE;
CREATE SEQUENCE product.product_status_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.product_status ALTER COLUMN id SET DEFAULT nextval('product.product_status_id_seq');
ALTER SEQUENCE product.product_status_id_seq OWNED BY product.product_status.id;

-------------------------------------------------------------------------------
-- table: product.download_queue
DROP TABLE IF EXISTS product.download_queue;
CREATE TABLE product.download_queue
(
	id varchar(50) NOT NULL,
	data_source varchar(255) NOT NULL,
	products json NOT NULL,
	destination varchar(512) NOT NULL,
	local_root varchar(512),
	tiles json,
	properties json
);
ALTER TABLE product.download_queue ADD CONSTRAINT PK_download_queue
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: product.raster_data_product
DROP TABLE IF EXISTS product.raster_data_product CASCADE;
CREATE TABLE product.raster_data_product
(
	id character varying(1000) NOT NULL,
	name varchar(250) NOT NULL,
	type_id integer NOT NULL,
	geometry geography(POLYGON, 4326) NOT NULL,
	coordinate_reference_system text NULL,
	location varchar NOT NULL,
	entry_point varchar NULL,
	sensor_type_id integer NOT NULL,
	acquisition_date timestamp NULL,
	pixel_type_id integer NOT NULL,
	product_type varchar(512) NOT NULL,
	width integer NOT NULL,
	height integer NOT NULL,
	approximate_size bigint NOT NULL,
	-- username varchar NULL,
	visibility_id integer NULL DEFAULT 2,
	status_id integer NULL,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE product.raster_data_product ADD CONSTRAINT PK_raster_data_product
	PRIMARY KEY (id);
ALTER TABLE product.raster_data_product ADD CONSTRAINT FK_raster_data_product_data_format
	FOREIGN KEY (type_id) REFERENCES product.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE product.raster_data_product ADD CONSTRAINT FK_raster_data_product_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES product.sensor_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE product.raster_data_product ADD CONSTRAINT FK_raster_data_product_pixel_type
	FOREIGN KEY (pixel_type_id) REFERENCES product.pixel_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE product.raster_data_product ADD CONSTRAINT FK_raster_data_product_visibility
	FOREIGN KEY (visibility_id) REFERENCES common.visibility (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE product.raster_data_product ADD CONSTRAINT FK_raster_data_product_product_status
   	FOREIGN KEY (status_id) REFERENCES product.product_status (id) ON DELETE No Action ON UPDATE No Action;

-- table: product.raster_data_product_refs
DROP TABLE IF EXISTS product.raster_data_product_refs CASCADE;
CREATE TABLE product.raster_data_product_refs
(
	product_id character varying(1000) NOT NULL,
	refs character varying(100) NOT NULL
);
ALTER TABLE product.raster_data_product_refs ADD CONSTRAINT PK_raster_data_product_refs
	PRIMARY KEY (product_id, refs);
ALTER TABLE product.raster_data_product_refs ADD CONSTRAINT FK_raster_data_product_refs_product
	FOREIGN KEY (product_id) REFERENCES product.raster_data_product (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: product.vector_data_product
DROP TABLE IF EXISTS product.vector_data_product CASCADE;
CREATE TABLE product.vector_data_product
(
	id character varying(1000) NOT NULL,
	name varchar(250) NOT NULL,
	type_id integer NOT NULL,
	geometry geography(POLYGON, 4326) NOT NULL,
	coordinate_reference_system text NULL,
	location varchar NOT NULL,
	entry_point varchar NULL,
	--username varchar NULL,
	visibility_id integer NULL DEFAULT 2,
	status_id integer NULL,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE product.vector_data_product ADD CONSTRAINT PK_vector_data_product
	PRIMARY KEY (id);
ALTER TABLE product.vector_data_product ADD CONSTRAINT FK_vector_data_product_data_format
	FOREIGN KEY (type_id) REFERENCES product.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE product.vector_data_product ADD CONSTRAINT FK_vector_data_product_visibility
	FOREIGN KEY (visibility_id) REFERENCES common.visibility (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE product.vector_data_product ADD CONSTRAINT FK_vector_data_product_product_status
	FOREIGN KEY (status_id) REFERENCES product.product_status (id) ON DELETE No Action ON UPDATE No Action;

-- table: product.vector_data_product_refs
DROP TABLE IF EXISTS product.vector_data_product_refs CASCADE;
CREATE TABLE product.vector_data_product_refs
(
	product_id character varying(1000) NOT NULL,
	refs character varying(100) NOT NULL
);
ALTER TABLE product.vector_data_product_refs ADD CONSTRAINT PK_vector_data_product_refs
	PRIMARY KEY (product_id, refs);
ALTER TABLE product.vector_data_product_refs ADD CONSTRAINT FK_vector_data_product_refs_product
	FOREIGN KEY (product_id) REFERENCES product.vector_data_product (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: product.auxiliary_data
CREATE TABLE product.auxiliary_data
(
    id character varying(1000) NOT NULL,
	location character varying NOT NULL,
	description character varying NOT NULL,
	username character varying NOT NULL,
	visibility_id integer NULL DEFAULT 2,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE product.auxiliary_data ADD CONSTRAINT PK_auxiliary_data
	PRIMARY KEY (id);
ALTER TABLE product.auxiliary_data ADD CONSTRAINT FK_auxiliary_data_visibility
	FOREIGN KEY (visibility_id) REFERENCES common.visibility (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: product.data_product_attributes
DROP TABLE IF EXISTS product.data_product_attributes CASCADE;
CREATE TABLE product.data_product_attributes
(
	data_product_id varchar(1000) NOT NULL,
	name varchar(1000) NOT NULL,
	value text NOT NULL
);
ALTER TABLE product.data_product_attributes ADD CONSTRAINT PK_data_product_attributes
	PRIMARY KEY (data_product_id, name);

-------------------------------------------------------------------------------
-- table: product.data_type
DROP TABLE IF EXISTS product.data_type CASCADE;
CREATE TABLE product.data_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE product.data_type ADD CONSTRAINT PK_data_type
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.data_type_id_seq CASCADE;
CREATE SEQUENCE product.data_type_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.data_type ALTER COLUMN id SET DEFAULT nextval('product.data_type_id_seq');
ALTER SEQUENCE product.data_type_id_seq OWNED BY product.data_type.id;

-------------------------------------------------------------------------------
-- table: product.naming_rule
DROP TABLE IF EXISTS product.naming_rule CASCADE;
CREATE TABLE product.naming_rule
(
    id integer NOT NULL,
    sensor varchar(50) NOT NULL,
    regex varchar(512) NOT NULL,
    description varchar(100) NOT NULL
);
ALTER TABLE product.naming_rule ADD CONSTRAINT PK_naming_rule
    PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS product.naming_rule_id_seq CASCADE;
CREATE SEQUENCE product.naming_rule_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.naming_rule ALTER COLUMN id SET DEFAULT nextval('product.naming_rule_id_seq');
ALTER SEQUENCE product.naming_rule_id_seq OWNED BY product.naming_rule.id;

-------------------------------------------------------------------------------
-- table: product.naming_rule_token
DROP TABLE IF EXISTS product.naming_rule_token CASCADE;
CREATE TABLE product.naming_rule_token
(
    naming_rule_id integer NOT NULL,
    token_name varchar(20) NOT NULL,
    matching_group_number integer NOT NULL,
    description varchar(100) NOT NULL
);
ALTER TABLE product.naming_rule_token ADD CONSTRAINT PK_naming_rule_token
    PRIMARY KEY (naming_rule_id, token_name);
ALTER TABLE product.naming_rule_token ADD CONSTRAINT FK_naming_rule_token_naming_rule
	FOREIGN KEY (naming_rule_id) REFERENCES product.naming_rule (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: product.data_sources_index
DROP TABLE IF EXISTS product.data_sources_index CASCADE;
CREATE TABLE product.data_sources_index
(
	id integer NOT NULL,
    data_source_name varchar(50) NOT NULL,
    sensor_name varchar(50) NOT NULL,
    from_year integer NOT NULL,
   	to_year integer NULL,
   	last_run timestamp NULL,
   	params json NULL
);
ALTER TABLE product.data_sources_index ADD CONSTRAINT PK_query
	PRIMARY KEY (data_source_name,sensor_name);
DROP SEQUENCE IF EXISTS product.data_sources_index_id_seq CASCADE;
CREATE SEQUENCE product.data_sources_index_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE product.data_sources_index ALTER COLUMN id SET DEFAULT nextval('product.data_sources_index_id_seq');
ALTER SEQUENCE product.data_sources_index_id_seq OWNED BY product.data_sources_index.id;
-------------------------------------------------------------------------------
-- table: product.data_sources_index_props
DROP TABLE IF EXISTS product.data_sources_index_props CASCADE;
CREATE TABLE product.data_sources_index_props
(
    data_source_id integer NOT NULL,
    footprint  geometry(Geometry,4326) NULL,
    acquisition_date timestamp NULL
);

ALTER TABLE IF EXISTS product.data_sources_index_props
    OWNER to tao;
ALTER TABLE IF EXISTS product.data_sources_index_props ADD CONSTRAINT info UNIQUE (data_source_id, footprint, acquisition_date);
-------------------------------------------------------------------------------
-- Index: ix_data_source_geom
DROP INDEX IF EXISTS public.ix_data_sources_geom;
CREATE INDEX IF NOT EXISTS ix_data_sources_geom
    ON product.data_sources_index_props USING gist (footprint)
TABLESPACE pg_default;