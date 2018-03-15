-------------------------------------------------------------------------------
-- table: data_format
DROP TABLE IF EXISTS tao.data_format CASCADE;

CREATE TABLE tao.data_format
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);

ALTER TABLE tao.data_format ADD CONSTRAINT PK_data_format
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: pixel_type
DROP TABLE IF EXISTS tao.pixel_type CASCADE;

CREATE TABLE tao.pixel_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);

ALTER TABLE tao.pixel_type ADD CONSTRAINT PK_pixel_type
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: sensor_type
DROP TABLE IF EXISTS tao.sensor_type CASCADE;

CREATE TABLE tao.sensor_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);

ALTER TABLE tao.sensor_type ADD CONSTRAINT PK_sensor_type
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: orbit_direction
DROP TABLE IF EXISTS tao.orbit_direction CASCADE;

CREATE TABLE tao.orbit_direction
(
	id integer NOT NULL,
	direction varchar(50) NOT NULL
);

ALTER TABLE tao.orbit_direction ADD CONSTRAINT PK_orbit_direction
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: polarisation_mode
DROP TABLE IF EXISTS tao.polarisation_mode CASCADE;

CREATE TABLE tao.polarisation_mode
(
	id integer NOT NULL,
	mode varchar(50) NOT NULL
);

ALTER TABLE tao.polarisation_mode ADD CONSTRAINT PK_polarisation_mode
	PRIMARY KEY (id);

	
-------------------------------------------------------------------------------
-- table: polarisation_channel
DROP TABLE IF EXISTS tao.polarisation_channel CASCADE;

CREATE TABLE tao.polarisation_channel
(
	id integer NOT NULL,
	channel varchar(50) NOT NULL
);

ALTER TABLE tao.polarisation_channel ADD CONSTRAINT PK_polarisation_channel
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: data_source_type
DROP TABLE IF EXISTS tao.data_source_type CASCADE;

CREATE TABLE tao.data_source_type
(
	id integer NOT NULL,
	type varchar(250) NOT NULL
);

ALTER TABLE tao.data_source_type ADD CONSTRAINT PK_data_source_type
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: data_source
DROP TABLE IF EXISTS tao.data_source CASCADE;

CREATE TABLE tao.data_source
(
	id integer NOT NULL,
	name varchar(250) NOT NULL,
	data_source_type_id integer NOT NULL,
	username varchar(50) NULL,
	password text NULL,
	auth_token text NULL,
	connection_string varchar(500) NULL,
	description text NULL,
	created timestamp NOT NULL,
	modified timestamp NULL,
	active boolean NULL
);

ALTER TABLE tao.data_source ADD CONSTRAINT PK_data_source
	PRIMARY KEY (id);

ALTER TABLE tao.data_source ADD CONSTRAINT FK_data_source_data_source_type
	FOREIGN KEY (data_source_type_id) REFERENCES tao.data_source_type (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: raster_data_product
DROP TABLE IF EXISTS tao.raster_data_product CASCADE;

CREATE TABLE tao.raster_data_product
(
	id character varying(1000) NOT NULL,
	name varchar(250) NOT NULL,
	type_id integer NOT NULL,
	geometry geography(POLYGON, 4326) NOT NULL,
	coordinate_reference_system text NULL,
	location varchar(512) NOT NULL,
	entry_point varchar(1000) NULL,
	sensor_type_id integer NOT NULL,
	acquisition_date timestamp NULL,
	pixel_type_id integer NOT NULL,
	product_type varchar(512) NOT NULL,
	width integer NOT NULL,
	height integer NOT NULL,
	approximate_size bigint NOT NULL,
	user_id integer NULL,
	data_source_id integer NULL,
	created timestamp NOT NULL DEFAULT now(),
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
	
ALTER TABLE tao.raster_data_product ADD CONSTRAINT FK_raster_data_product_data_source
	FOREIGN KEY (data_source_id) REFERENCES tao.data_source (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: vector_data_product
DROP TABLE IF EXISTS tao.vector_data_product CASCADE;

CREATE TABLE tao.vector_data_product
(
	id character varying(1000) NOT NULL,
	name varchar(250) NOT NULL,
	type_id integer NOT NULL,
	geometry geography(POLYGON, 4326) NOT NULL,
	coordinate_reference_system text NULL,
	location varchar(512) NOT NULL,
	entry_point varchar(1000) NULL,
	user_id integer NULL,
	data_source_id integer NULL,
	created timestamp NOT NULL DEFAULT now(),
	modified timestamp NULL
);

ALTER TABLE tao.vector_data_product ADD CONSTRAINT PK_vector_data_product
	PRIMARY KEY (id);

ALTER TABLE tao.vector_data_product ADD CONSTRAINT FK_vector_data_product_data_format
	FOREIGN KEY (type_id) REFERENCES tao.data_format (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.vector_data_product ADD CONSTRAINT FK_vector_data_product_data_source
	FOREIGN KEY (data_source_id) REFERENCES tao.data_source (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: data_product_attributes
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
-- table: data_type
DROP TABLE IF EXISTS tao.data_type CASCADE;

CREATE TABLE tao.data_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);

ALTER TABLE tao.data_type ADD CONSTRAINT PK_data_type
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: query_parameter
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
-- table: data_query
DROP TABLE IF EXISTS tao.data_query CASCADE;

CREATE TABLE tao.data_query
(
	id integer NOT NULL,
	name varchar(50) NOT NULL,
	data_source_id integer NOT NULL,
	query_text text NOT NULL,
	page_size integer,
	page_number integer,
	"limit" integer,
	timeout bigint
);

ALTER TABLE tao.data_query ADD CONSTRAINT PK_data_query
	PRIMARY KEY (id);
	
ALTER TABLE tao.data_query ADD CONSTRAINT FK_data_query_data_source
	FOREIGN KEY (data_source_id) REFERENCES tao.data_source (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: data_query_parameters
DROP TABLE IF EXISTS tao.data_query_parameters CASCADE;

CREATE TABLE tao.data_query_parameters
(
	data_query_id integer NOT NULL,
	query_parameter_id integer NOT NULL,
	optional boolean NOT NULL,
    min_value varchar(250),
    max_value varchar(250),
    value varchar(250)
);

ALTER TABLE tao.data_query_parameters ADD CONSTRAINT PK_data_query_parameters
	PRIMARY KEY (data_query_id, query_parameter_id);	

ALTER TABLE tao.data_query_parameters ADD CONSTRAINT FK_data_query_parameters_data_query
	FOREIGN KEY (data_query_id) REFERENCES tao.data_query (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.data_query_parameters ADD CONSTRAINT FK_data_query_parameters_query_parameter
	FOREIGN KEY (query_parameter_id) REFERENCES tao.query_parameter (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: user_data_source_connection
DROP TABLE IF EXISTS tao.user_data_source_connection CASCADE;

CREATE TABLE tao.user_data_source_connection
(
	user_id integer NOT NULL,
	data_source_id integer NOT NULL,
	username varchar(50) NULL,
	password text NULL,
	created timestamp NOT NULL,
	modified timestamp NULL
);

ALTER TABLE tao.user_data_source_connection ADD CONSTRAINT PK_user_data_source_connection
	PRIMARY KEY (user_id, data_source_id);

ALTER TABLE tao.user_data_source_connection ADD CONSTRAINT FK_user_data_source_connection_user
	FOREIGN KEY (user_id) REFERENCES tao."user" (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.user_data_source_connection ADD CONSTRAINT FK_user_data_source_connection_data_source
	FOREIGN KEY (data_source_id) REFERENCES tao.data_source (id) ON DELETE No Action ON UPDATE No Action;
	

-------------------------------------------------------------------------------
-- table: user_data_query
DROP TABLE IF EXISTS tao.user_data_query CASCADE;

CREATE TABLE tao.user_data_query
(
	user_id integer NOT NULL,
	data_source_id integer NOT NULL,
	job_id bigint NOT NULL,
	query_parameter_id integer NOT NULL,
	query_parameter_value varchar(500)
);

ALTER TABLE tao.user_data_query ADD CONSTRAINT PK_user_data_query
	PRIMARY KEY (job_id, query_parameter_id);

ALTER TABLE tao.user_data_query ADD CONSTRAINT FK_user_data_query_user
	FOREIGN KEY (user_id) REFERENCES tao."user" (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.user_data_query ADD CONSTRAINT FK_user_data_query_data_source
	FOREIGN KEY (data_source_id) REFERENCES tao.data_source (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.user_data_query ADD CONSTRAINT FK_user_data_query_job
	FOREIGN KEY (job_id) REFERENCES tao.job (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.user_data_query ADD CONSTRAINT FK_user_data_query_query_parameter
	FOREIGN KEY (query_parameter_id) REFERENCES tao.query_parameter (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: notification
DROP TABLE IF EXISTS tao.notification CASCADE;

CREATE TABLE tao.notification
(
    timestamp bigint NOT NULL,
	username character varying NOT NULL,
	read boolean NULL,
	data character varying NOT NULL
);

ALTER TABLE tao.notification ADD CONSTRAINT PK_notification PRIMARY KEY (timestamp, username);

--ALTER TABLE tao.notification ADD CONSTRAINT FK_notification_user
--	FOREIGN KEY (user_id) REFERENCES tao."user" (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: source_descriptor
DROP TABLE IF EXISTS tao.source_descriptor CASCADE;

CREATE TABLE tao.source_descriptor
(
    id varchar(512) NOT NULL,
	parent_id varchar(512) NOT NULL,
	constraints text NULL,

	data_format_id integer NOT NULL,
    geometry geography(POLYGON, 4326) NOT NULL,
    coordinate_reference_system text NULL,
    sensor_type_id integer NOT NULL,
    dimension json NOT NULL,
    location varchar(512) NOT NULL
);

ALTER TABLE tao.source_descriptor ADD CONSTRAINT PK_source_descriptor PRIMARY KEY (id);

ALTER TABLE tao.source_descriptor ADD CONSTRAINT FK_source_descriptor_processing_component
	FOREIGN KEY (parent_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.source_descriptor ADD CONSTRAINT FK_data_descriptor_data_format
	FOREIGN KEY (data_format_id) REFERENCES tao.data_format (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.source_descriptor ADD CONSTRAINT FK_data_descriptor_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES tao.sensor_type (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: target_descriptor
DROP TABLE IF EXISTS tao.target_descriptor CASCADE;

CREATE TABLE tao.target_descriptor
(
    id varchar(512) NOT NULL,
	parent_id varchar(512) NOT NULL,
	constraints text NULL,

	data_format_id integer NOT NULL,
    geometry geography(POLYGON, 4326) NOT NULL,
    coordinate_reference_system text NULL,
    sensor_type_id integer NOT NULL,
    dimension json NOT NULL,
    location varchar(512) NOT NULL
);

ALTER TABLE tao.target_descriptor ADD CONSTRAINT PK_target_descriptor PRIMARY KEY (id);

ALTER TABLE tao.target_descriptor ADD CONSTRAINT FK_target_descriptor_processing_component
	FOREIGN KEY (parent_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.target_descriptor ADD CONSTRAINT FK_data_descriptor_data_format
	FOREIGN KEY (data_format_id) REFERENCES tao.data_format (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.target_descriptor ADD CONSTRAINT FK_data_descriptor_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES tao.sensor_type (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: component_link
DROP TABLE IF EXISTS tao.component_link CASCADE;

CREATE TABLE tao.component_link
(
    graph_node_id bigint NOT NULL,
    source_descriptor_id varchar(512) NOT NULL,
    target_descriptor_id varchar(512) NOT NULL
);

ALTER TABLE tao.component_link ADD CONSTRAINT PK_component_link PRIMARY KEY (graph_node_id, source_descriptor_id, target_descriptor_id);

ALTER TABLE tao.component_link ADD CONSTRAINT FK_component_link_graph_node
	FOREIGN KEY (graph_node_id) REFERENCES tao.graph_node (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.component_link ADD CONSTRAINT FK_component_link_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES tao.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.component_link ADD CONSTRAINT FK_component_link_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES tao.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: component_sources
DROP TABLE IF EXISTS tao.component_sources CASCADE;

CREATE TABLE tao.component_sources
(
	processing_component_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);

ALTER TABLE tao.component_sources ADD CONSTRAINT PK_component_sources
	PRIMARY KEY (processing_component_id, source_descriptor_id);

ALTER TABLE tao.component_sources ADD CONSTRAINT FK_component_sources_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.component_sources ADD CONSTRAINT FK_component_sources_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES tao.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: component_targets
DROP TABLE IF EXISTS tao.component_targets CASCADE;

CREATE TABLE tao.component_targets
(
	processing_component_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);

ALTER TABLE tao.component_targets ADD CONSTRAINT PK_component_targets
	PRIMARY KEY (processing_component_id, target_descriptor_id);

ALTER TABLE tao.component_targets ADD CONSTRAINT FK_component_targets_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.component_targets ADD CONSTRAINT FK_component_targets_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES tao.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;



-------------------------------------------------------------------------------
-- table: fetch_mode
DROP TABLE IF EXISTS tao.fetch_mode CASCADE;

CREATE TABLE tao.fetch_mode
(
	id integer NOT NULL,
	fetch_mode varchar(50) NOT NULL
);

ALTER TABLE tao.fetch_mode ADD CONSTRAINT PK_fetch_mode
	PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: data_source_component
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
	source_cardinality integer NOT NULL,
    target_cardinality integer NOT NULL,

    sensor_name varchar(1024) NOT NULL,
	data_source_name  varchar(512) NOT NULL,
	username varchar(512) NULL,
	password bytea NULL,
	fetch_mode_id integer NOT NULL,
	overridden_parameters json NULL,
	created timestamp NOT NULL DEFAULT now(),
    modified timestamp NULL
);

ALTER TABLE tao.data_source_component ADD CONSTRAINT PK_data_source_component
	PRIMARY KEY (id);

ALTER TABLE tao.data_source_component ADD CONSTRAINT FK_data_source_component_fetch_mode
	FOREIGN KEY (fetch_mode_id) REFERENCES tao.fetch_mode (id) ON DELETE No Action ON UPDATE No Action;

