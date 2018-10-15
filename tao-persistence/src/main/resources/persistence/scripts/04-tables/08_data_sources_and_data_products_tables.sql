-------------------------------------------------------------------------------
-- table: tao.data_format
DROP TABLE IF EXISTS tao.data_format CASCADE;
CREATE TABLE tao.data_format
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE tao.data_format ADD CONSTRAINT PK_data_format
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.pixel_type
DROP TABLE IF EXISTS tao.pixel_type CASCADE;
CREATE TABLE tao.pixel_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE tao.pixel_type ADD CONSTRAINT PK_pixel_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.sensor_type
DROP TABLE IF EXISTS tao.sensor_type CASCADE;
CREATE TABLE tao.sensor_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE tao.sensor_type ADD CONSTRAINT PK_sensor_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.orbit_direction
DROP TABLE IF EXISTS tao.orbit_direction CASCADE;
CREATE TABLE tao.orbit_direction
(
	id integer NOT NULL,
	direction varchar(50) NOT NULL
);
ALTER TABLE tao.orbit_direction ADD CONSTRAINT PK_orbit_direction
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.polarisation_mode
DROP TABLE IF EXISTS tao.polarisation_mode CASCADE;
CREATE TABLE tao.polarisation_mode
(
	id integer NOT NULL,
	mode varchar(50) NOT NULL
);
ALTER TABLE tao.polarisation_mode ADD CONSTRAINT PK_polarisation_mode
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.polarisation_channel
DROP TABLE IF EXISTS tao.polarisation_channel CASCADE;
CREATE TABLE tao.polarisation_channel
(
	id integer NOT NULL,
	channel varchar(50) NOT NULL
);
ALTER TABLE tao.polarisation_channel ADD CONSTRAINT PK_polarisation_channel
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.raster_data_product
DROP TABLE IF EXISTS tao.raster_data_product CASCADE;
CREATE TABLE tao.raster_data_product
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
	username varchar NULL,
	visibility_id integer NULL DEFAULT 2,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE tao.raster_data_product ADD CONSTRAINT PK_raster_data_product
	PRIMARY KEY (id);
ALTER TABLE tao.raster_data_product ADD CONSTRAINT FK_raster_data_product_data_format
	FOREIGN KEY (type_id) REFERENCES tao.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.raster_data_product ADD CONSTRAINT FK_raster_data_product_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES tao.sensor_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.raster_data_product ADD CONSTRAINT FK_raster_data_product_pixel_type
	FOREIGN KEY (pixel_type_id) REFERENCES tao.pixel_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.raster_data_product ADD CONSTRAINT FK_raster_data_product_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.visibility (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.vector_data_product
DROP TABLE IF EXISTS tao.vector_data_product CASCADE;
CREATE TABLE tao.vector_data_product
(
	id character varying(1000) NOT NULL,
	name varchar(250) NOT NULL,
	type_id integer NOT NULL,
	geometry geography(POLYGON, 4326) NOT NULL,
	coordinate_reference_system text NULL,
	location varchar NOT NULL,
	entry_point varchar NULL,
	username varchar NULL,
	visibility_id integer NULL DEFAULT 2,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE tao.vector_data_product ADD CONSTRAINT PK_vector_data_product
	PRIMARY KEY (id);
ALTER TABLE tao.vector_data_product ADD CONSTRAINT FK_vector_data_product_data_format
	FOREIGN KEY (type_id) REFERENCES tao.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.vector_data_product ADD CONSTRAINT FK_vector_data_product_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.visibility (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.auxiliary_data
CREATE TABLE tao.auxiliary_data
(
    id character varying(1000) NOT NULL,
	location character varying NOT NULL,
	description character varying NOT NULL,
	username character varying NOT NULL,
	visibility_id integer NULL DEFAULT 2,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE tao.auxiliary_data ADD CONSTRAINT PK_auxiliary_data
	PRIMARY KEY (id);
ALTER TABLE tao.auxiliary_data ADD CONSTRAINT FK_auxiliary_data_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.visibility (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.data_product_attributes
DROP TABLE IF EXISTS tao.data_product_attributes CASCADE;
CREATE TABLE tao.data_product_attributes
(
	data_product_id varchar(1000) NOT NULL,
	name varchar(1000) NOT NULL,
	value text NOT NULL
);
ALTER TABLE tao.data_product_attributes ADD CONSTRAINT PK_data_product_attributes
	PRIMARY KEY (data_product_id, name);

-------------------------------------------------------------------------------
-- table: tao.data_type
DROP TABLE IF EXISTS tao.data_type CASCADE;
CREATE TABLE tao.data_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE tao.data_type ADD CONSTRAINT PK_data_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.query_parameter
DROP TABLE IF EXISTS tao.query_parameter CASCADE;
CREATE TABLE tao.query_parameter
(
	id integer NOT NULL,
	data_type_id integer NOT NULL,
	name varchar(250) NOT NULL
);
ALTER TABLE tao.query_parameter ADD CONSTRAINT PK_query_parameter
	PRIMARY KEY (id);
ALTER TABLE tao.query_parameter ADD CONSTRAINT FK_query_parameter_data_type
	FOREIGN KEY (data_type_id) REFERENCES tao.data_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.user_data_query
DROP TABLE IF EXISTS tao.user_data_query CASCADE;
CREATE TABLE tao.user_data_query
(
    id bigint NOT NULL,
    job_id bigint NOT NULL,
	query_id integer NOT NULL
);
ALTER TABLE tao.user_data_query ADD CONSTRAINT PK_user_data_query
	PRIMARY KEY (job_id, query_id);
ALTER TABLE tao.user_data_query ADD CONSTRAINT FK_user_data_query_query
	FOREIGN KEY (query_id) REFERENCES tao.query (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.user_data_query ADD CONSTRAINT FK_user_data_query_job
	FOREIGN KEY (job_id) REFERENCES tao.job (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.notification
DROP TABLE IF EXISTS tao.notification CASCADE;
CREATE TABLE tao.notification
(
    timestamp bigint NOT NULL,
    topic character varying NOT NULL,
	username character varying NOT NULL,
	read boolean NULL,
	data character varying NOT NULL
);
ALTER TABLE tao.notification ADD CONSTRAINT PK_notification PRIMARY KEY (timestamp, username);
CREATE INDEX IX_notification ON tao.notification
    USING btree (topic COLLATE pg_catalog."default", username COLLATE pg_catalog."default");

-------------------------------------------------------------------------------
-- table: tao.source_descriptor
DROP TABLE IF EXISTS tao.source_descriptor CASCADE;
CREATE TABLE tao.source_descriptor
(
    id varchar(512) NOT NULL DEFAULT(uuid_generate_v4()),
	parent_id varchar(512) NOT NULL,
	name varchar(512) NOT NULL,
	cardinality integer NOT NULL,
	constraints text NULL,
	data_format_id integer NOT NULL,
    geometry geography(POLYGON, 4326) NULL,
    coordinate_reference_system text NULL,
    sensor_type_id integer NULL,
    dimension json NULL,
    location varchar NULL
);
ALTER TABLE tao.source_descriptor ADD CONSTRAINT PK_source_descriptor PRIMARY KEY (id);
ALTER TABLE tao.source_descriptor ADD CONSTRAINT FK_data_descriptor_data_format
	FOREIGN KEY (data_format_id) REFERENCES tao.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.source_descriptor ADD CONSTRAINT FK_data_descriptor_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES tao.sensor_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.target_descriptor
DROP TABLE IF EXISTS tao.target_descriptor CASCADE;
CREATE TABLE tao.target_descriptor
(
    id varchar(512) NOT NULL DEFAULT(uuid_generate_v4()),
    parent_id varchar(512) NOT NULL,
    name varchar(512) NOT NULL,
    cardinality integer NOT NULL,
	constraints text NULL,
	data_format_id integer NOT NULL,
    geometry geography(POLYGON, 4326) NULL,
    coordinate_reference_system text NULL,
    sensor_type_id integer NULL,
    dimension json NULL,
    location varchar NULL
);
ALTER TABLE tao.target_descriptor ADD CONSTRAINT PK_target_descriptor PRIMARY KEY (id);
ALTER TABLE tao.target_descriptor ADD CONSTRAINT FK_data_descriptor_data_format
	FOREIGN KEY (data_format_id) REFERENCES tao.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.target_descriptor ADD CONSTRAINT FK_data_descriptor_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES tao.sensor_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.component_link
DROP TABLE IF EXISTS tao.component_link CASCADE;
CREATE TABLE tao.component_link
(
    target_graph_node_id bigint NOT NULL,
    source_descriptor_id varchar(512) NOT NULL,
    source_graph_node_id bigint NOT NULL,
    target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE tao.component_link ADD CONSTRAINT PK_component_link PRIMARY KEY (source_graph_node_id, target_graph_node_id, source_descriptor_id, target_descriptor_id);
ALTER TABLE tao.component_link ADD CONSTRAINT FK_component_link_graph_node_1
	FOREIGN KEY (source_graph_node_id) REFERENCES tao.graph_node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.component_link ADD CONSTRAINT FK_component_link_graph_node_2
	FOREIGN KEY (target_graph_node_id) REFERENCES tao.graph_node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.component_link ADD CONSTRAINT FK_component_link_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES tao.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.component_link ADD CONSTRAINT FK_component_link_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES tao.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.processing_component_sources
DROP TABLE IF EXISTS tao.processing_component_sources CASCADE;
CREATE TABLE tao.processing_component_sources
(
	processing_component_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE tao.processing_component_sources ADD CONSTRAINT PK_processing_component_sources
	PRIMARY KEY (processing_component_id, source_descriptor_id);
ALTER TABLE tao.processing_component_sources ADD CONSTRAINT FK_processing_component_sources_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_component_sources ADD CONSTRAINT FK_processing_component_sources_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES tao.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.processing_component_targets
DROP TABLE IF EXISTS tao.processing_component_targets CASCADE;
CREATE TABLE tao.processing_component_targets
(
	processing_component_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE tao.processing_component_targets ADD CONSTRAINT PK_processing_component_targets
	PRIMARY KEY (processing_component_id, target_descriptor_id);
ALTER TABLE tao.processing_component_targets ADD CONSTRAINT FK_processing_component_targets_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_component_targets ADD CONSTRAINT FK_processing_component_targets_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES tao.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.group_component_sources
DROP TABLE IF EXISTS tao.group_component_sources CASCADE;
CREATE TABLE tao.group_component_sources
(
	group_component_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE tao.group_component_sources ADD CONSTRAINT PK_group_component_sources
	PRIMARY KEY (group_component_id, source_descriptor_id);
ALTER TABLE tao.group_component_sources ADD CONSTRAINT FK_group_component_sources_group_component
	FOREIGN KEY (group_component_id) REFERENCES tao.group_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.group_component_sources ADD CONSTRAINT FK_group_component_sources_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES tao.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.group_component_targets
DROP TABLE IF EXISTS tao.group_component_targets CASCADE;
CREATE TABLE tao.group_component_targets
(
	group_component_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE tao.group_component_targets ADD CONSTRAINT PK_group_component_targets
	PRIMARY KEY (group_component_id, target_descriptor_id);
ALTER TABLE tao.group_component_targets ADD CONSTRAINT FK_group_component_targets_group_component
	FOREIGN KEY (group_component_id) REFERENCES tao.group_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.group_component_targets ADD CONSTRAINT FK_group_component_targets_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES tao.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.fetch_mode
DROP TABLE IF EXISTS tao.fetch_mode CASCADE;
CREATE TABLE tao.fetch_mode
(
	id integer NOT NULL,
	fetch_mode varchar(50) NOT NULL
);
ALTER TABLE tao.fetch_mode ADD CONSTRAINT PK_fetch_mode
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.data_source_component
DROP TABLE IF EXISTS tao.data_source_component CASCADE;
CREATE TABLE tao.data_source_component
(
	id varchar(512) NOT NULL,
	label varchar(250) NOT NULL,
	version varchar(50) NOT NULL,
	description text NOT NULL,
	authors varchar(1024) NOT NULL,
	copyright text NOT NULL,
	node_affinity varchar(250) NULL,
    sensor_name varchar(1024) NOT NULL,
	data_source_name  varchar(512) NOT NULL,
	fetch_mode_id integer NOT NULL,
	max_retries integer NULL DEFAULT 3,
	is_system boolean NOT NULL DEFAULT false,
	tags text NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL
);
ALTER TABLE tao.data_source_component ADD CONSTRAINT PK_data_source_component
	PRIMARY KEY (id);
ALTER TABLE tao.data_source_component ADD CONSTRAINT FK_data_source_component_fetch_mode
	FOREIGN KEY (fetch_mode_id) REFERENCES tao.fetch_mode (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.data_source_component_sources
DROP TABLE IF EXISTS tao.data_source_component_sources CASCADE;
CREATE TABLE tao.data_source_component_sources
(
	data_source_component_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE tao.data_source_component_sources ADD CONSTRAINT PK_data_source_component_sources
	PRIMARY KEY (data_source_component_id, source_descriptor_id);
ALTER TABLE tao.data_source_component_sources ADD CONSTRAINT FK_data_source_component_sources_data_source_component
	FOREIGN KEY (data_source_component_id) REFERENCES tao.data_source_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.data_source_component_sources ADD CONSTRAINT FK_data_source_component_sources_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES tao.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.data_source_component_targets
DROP TABLE IF EXISTS tao.data_source_component_targets CASCADE;
CREATE TABLE tao.data_source_component_targets
(
	data_source_component_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE tao.data_source_component_targets ADD CONSTRAINT PK_data_source_component_targets
	PRIMARY KEY (data_source_component_id, target_descriptor_id);
ALTER TABLE tao.data_source_component_targets ADD CONSTRAINT FK_data_source_component_targets_processing_component
	FOREIGN KEY (data_source_component_id) REFERENCES tao.data_source_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.data_source_component_targets ADD CONSTRAINT FK_data_source_component_targets_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES tao.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.user_data_source_connection
DROP TABLE IF EXISTS tao.user_data_source_connection CASCADE;
CREATE TABLE tao.user_data_source_connection
(
    id bigserial NOT NULL,
	user_id varchar(50) NOT NULL,
    data_source_id varchar(512) NOT NULL,
	username varchar(50) NULL,
	password text NULL,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE tao.user_data_source_connection ADD CONSTRAINT PK_user_data_source_connection
	PRIMARY KEY (id);
ALTER TABLE tao.user_data_source_connection ADD CONSTRAINT U_user_data_source_connection UNIQUE (user_id, data_source_id);
ALTER TABLE tao.user_data_source_connection ADD CONSTRAINT FK_user_data_source_connection_user
	FOREIGN KEY (user_id) REFERENCES tao."user" (username) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.user_data_source_connection ADD CONSTRAINT FK_user_data_source_connection_data_source
	FOREIGN KEY (data_source_id) REFERENCES tao.data_source_component(id) ON DELETE No Action ON UPDATE No Action;

