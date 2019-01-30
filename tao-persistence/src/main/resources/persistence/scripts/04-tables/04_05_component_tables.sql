-------------------------------------------------------------------------------
-- table: parameter_type
DROP TABLE IF EXISTS component.parameter_type CASCADE;
CREATE TABLE component.parameter_type
(
	id integer NOT NULL,
	type varchar(250) NOT NULL
);
ALTER TABLE component.parameter_type ADD CONSTRAINT PK_parameter_type
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS component.parameter_type_id_seq CASCADE;
CREATE SEQUENCE component.parameter_type_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE component.parameter_type ALTER COLUMN id SET DEFAULT nextval('component.parameter_type_id_seq');
ALTER SEQUENCE component.parameter_type_id_seq OWNED BY component.parameter_type.id;

-------------------------------------------------------------------------------
-- table: component_visibility
DROP TABLE IF EXISTS component.component_visibility CASCADE;
CREATE TABLE component.component_visibility
(
	id integer NOT NULL,
	visibility varchar(50) NOT NULL
);
ALTER TABLE component.component_visibility ADD CONSTRAINT PK_component_visibility
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS component.component_visibility_id_seq CASCADE;
CREATE SEQUENCE component.component_visibility_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE component.component_visibility ALTER COLUMN id SET DEFAULT nextval('component.component_visibility_id_seq');
ALTER SEQUENCE component.component_visibility_id_seq OWNED BY component.component_visibility.id;
	
-------------------------------------------------------------------------------
-- table: processing_component_type
DROP TABLE IF EXISTS component.processing_component_type CASCADE;
CREATE TABLE component.processing_component_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE component.processing_component_type ADD CONSTRAINT PK_processing_component_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: template_type
DROP TABLE IF EXISTS component.template_type CASCADE;
CREATE TABLE component.template_type
(
	id integer NOT NULL,
	type varchar(250) NOT NULL
);
ALTER TABLE component.template_type ADD CONSTRAINT PK_template_type
	PRIMARY KEY (id);
----- table: component.template_type --------------------------------------------------------------------
DROP SEQUENCE IF EXISTS component.template_type_id_seq CASCADE;
CREATE SEQUENCE component.template_type_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE component.template_type ALTER COLUMN id SET DEFAULT nextval('component.template_type_id_seq');
ALTER SEQUENCE component.template_type_id_seq OWNED BY component.template_type.id;

-------------------------------------------------------------------------------
-- table: component.fetch_mode
DROP TABLE IF EXISTS component.fetch_mode CASCADE;
CREATE TABLE component.fetch_mode
(
	id integer NOT NULL,
	fetch_mode varchar(50) NOT NULL
);
ALTER TABLE component.fetch_mode ADD CONSTRAINT PK_fetch_mode
	PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS component.fetch_mode_id_seq CASCADE;
CREATE SEQUENCE component.fetch_mode_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE component.fetch_mode ALTER COLUMN id SET DEFAULT nextval('component.fetch_mode_id_seq');
ALTER SEQUENCE component.fetch_mode_id_seq OWNED BY component.fetch_mode.id;

-------------------------------------------------------------------------------
-- table: container
DROP TABLE IF EXISTS component.container CASCADE;
CREATE TABLE component.container
(
	id varchar(1024) NOT NULL,
	name varchar(1024) NOT NULL,
	tag varchar(1024) NOT NULL,
	application_path varchar(1024) NULL,
	logo_image varchar NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL
);
ALTER TABLE component.container ADD CONSTRAINT PK_container
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: container_applications
DROP TABLE IF EXISTS component.container_applications CASCADE;
CREATE TABLE component.container_applications
(
	container_id varchar(1024) NOT NULL,
	name varchar(1024) NOT NULL,
	path varchar(1024) NULL,
	parallel_flag varchar(50) NULL,
	memory integer NULL
);
ALTER TABLE component.container_applications ADD CONSTRAINT PK_container_applications
	PRIMARY KEY (container_id, name);
ALTER TABLE component.container_applications ADD CONSTRAINT FK_container_applications_container
	FOREIGN KEY (container_id) REFERENCES component.container (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: processing_component
DROP TABLE IF EXISTS component.processing_component CASCADE;
CREATE TABLE component.processing_component
(
	id varchar(512) NOT NULL,
	label varchar NOT NULL,
	version varchar(50) NOT NULL,
	description text NOT NULL,
	authors varchar(1024) NOT NULL,
	copyright text NOT NULL,
	node_affinity varchar(250) NULL,
    container_id varchar(1024) NULL,
	main_tool_file_location varchar(512) NOT NULL,
	working_directory varchar(512) NULL,
	template_type_id integer NOT NULL,
	owner_user varchar NULL,
	visibility_id integer NOT NULL,
	multi_thread boolean NULL DEFAULT false,
	template_contents text NULL,
	component_type_id integer NULL DEFAULT 1,
	tags text NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL,
	active boolean NULL DEFAULT true
);
ALTER TABLE component.processing_component ADD CONSTRAINT PK_processing_component
	PRIMARY KEY (id);
ALTER TABLE component.processing_component ADD CONSTRAINT FK_processing_component_template_type
	FOREIGN KEY (template_type_id) REFERENCES component.template_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_component ADD CONSTRAINT FK_processing_component_user
	FOREIGN KEY (owner_user) REFERENCES usr.user (username) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_component ADD CONSTRAINT FK_processing_component_component_visibility
	FOREIGN KEY (visibility_id) REFERENCES component.component_visibility (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_component ADD CONSTRAINT FK_processing_component_processing_component_type
	FOREIGN KEY (component_type_id) REFERENCES component.processing_component_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_component ADD CONSTRAINT FK_processing_component_container
	FOREIGN KEY (container_id) REFERENCES component.container (id) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS component.processing_component_id_seq CASCADE;
CREATE SEQUENCE component.processing_component_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE component.processing_component ALTER COLUMN id SET DEFAULT nextval('component.processing_component_id_seq');
ALTER SEQUENCE component.processing_component_id_seq OWNED BY component.processing_component.id;

-------------------------------------------------------------------------------
-- table: processing_parameter
DROP TABLE IF EXISTS component.processing_parameter CASCADE;
CREATE TABLE component.processing_parameter
(
	id varchar(512) NOT NULL,
	name varchar(512) NOT NULL,
    type_id integer NOT NULL,
    label varchar(512) NOT NULL,
    data_type varchar(512) NOT NULL,
    default_value varchar(250) NULL,
    description text NULL,
    unit varchar(250) NULL,
    format varchar(250) NULL,
    value_set text NULL,
    not_null boolean NULL,
    -- special column used by JPA to distinguish which type of object is stored in one row (since this table holds 2 types of entities)
    discriminator integer NOT NULL
);
ALTER TABLE component.processing_parameter ADD CONSTRAINT PK_processing_parameter
	PRIMARY KEY (id);
ALTER TABLE component.processing_parameter ADD CONSTRAINT FK_processing_parameter_parameter_type
	FOREIGN KEY (type_id) REFERENCES component.parameter_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.condition
DROP TABLE IF EXISTS component.condition CASCADE;
CREATE TABLE component.condition
(
    id integer,
    description varchar(250)
);
ALTER TABLE component.condition ADD CONSTRAINT PK_condition
    PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: processing_parameter_dependencies
DROP TABLE IF EXISTS component.processing_parameter_dependencies CASCADE;
CREATE TABLE component.processing_parameter_dependencies
(
    parameter_id varchar(512) NOT NULL,
    dependant_parameter_id varchar(512) NOT NULL,
    dependency_condition_id integer NOT NULL,
    dependant_parameter_value varchar(250) NULL
);
ALTER TABLE component.processing_parameter_dependencies ADD CONSTRAINT PK_processing_parameter_dependencies
    PRIMARY KEY (parameter_id, dependant_parameter_id);
ALTER TABLE component.processing_parameter_dependencies ADD CONSTRAINT FK_processing_parameter_dependencies_processing_parameter_1
    FOREIGN KEY (parameter_id) REFERENCES component.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_parameter_dependencies ADD CONSTRAINT FK_processing_parameter_dependencies_processing_parameter_2
    FOREIGN KEY (dependant_parameter_id) REFERENCES component.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_parameter_dependencies ADD CONSTRAINT FK_processing_parameter_dependencies_condition
    FOREIGN KEY (dependency_condition_id) REFERENCES component.condition (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component_parameters
DROP TABLE IF EXISTS component.component_parameters CASCADE;
CREATE TABLE component.component_parameters
(
	processing_component_id varchar(512) NOT NULL,
	processing_parameter_id varchar(512) NOT NULL
);
ALTER TABLE component.component_parameters ADD CONSTRAINT PK_component_parameters
	PRIMARY KEY (processing_component_id, processing_parameter_id);
ALTER TABLE component.component_parameters ADD CONSTRAINT FK_component_parameters_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES component.processing_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.component_parameters ADD CONSTRAINT FK_component_parameters_processing_parameter
	FOREIGN KEY (processing_parameter_id) REFERENCES component.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: template_parameter_parameters
DROP TABLE IF EXISTS component.template_parameter_parameters CASCADE;
CREATE TABLE component.template_parameter_parameters
(
    template_parameter_id varchar(512) NOT NULL,
	regular_parameter_id varchar(512) NOT NULL
);
ALTER TABLE component.template_parameter_parameters ADD CONSTRAINT PK_template_parameter_parameters
	PRIMARY KEY (template_parameter_id, regular_parameter_id);
ALTER TABLE component.template_parameter_parameters ADD CONSTRAINT FK_template_parameter_parameters_processing_parameter_01
	FOREIGN KEY (template_parameter_id) REFERENCES component.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.template_parameter_parameters ADD CONSTRAINT FK_template_parameter_parameters_processing_parameter_02
	FOREIGN KEY (regular_parameter_id) REFERENCES component.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component_parameter_values_set
DROP TABLE IF EXISTS component.component_parameter_values_set CASCADE;
CREATE TABLE component.component_parameter_values_set
(
	parameter_id varchar(512) NOT NULL,
	processing_component_id varchar(512) NOT NULL,
	parameter_value varchar(250) NOT NULL
);
ALTER TABLE component.component_parameter_values_set ADD CONSTRAINT PK_component_parameter_values_set
	PRIMARY KEY (parameter_id, processing_component_id, parameter_value);
ALTER TABLE component.component_parameter_values_set ADD CONSTRAINT FK_component_parameter_values_set_processing_parameter
	FOREIGN KEY (parameter_id) REFERENCES component.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.component_parameter_values_set ADD CONSTRAINT FK_component_parameter_values_set_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES component.processing_component (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component_variables
DROP TABLE IF EXISTS component.component_variables CASCADE;
CREATE TABLE component.component_variables
(
	processing_component_id varchar(512) NOT NULL,
	key varchar(512) NOT NULL,
	value varchar(512) NOT NULL
);
ALTER TABLE component.component_variables ADD CONSTRAINT PK_component_variables
	PRIMARY KEY (processing_component_id, key);
ALTER TABLE component.component_variables ADD CONSTRAINT FK_component_variables_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES component.processing_component (id) ON DELETE No Action ON UPDATE No Action;
	
-------------------------------------------------------------------------------
-- table: group_component
DROP TABLE IF EXISTS component.group_component CASCADE;
CREATE TABLE component.group_component
(
	id varchar(512) NOT NULL,
	label varchar NOT NULL,
	version varchar(50) NOT NULL,
	description text NOT NULL,
	authors varchar(1024) NOT NULL,
	copyright text NOT NULL,
	node_affinity varchar(250) NULL,
	owner_user_id integer NULL,
	visibility_id integer NOT NULL,
	parallelism integer NULL DEFAULT 1,
	tags text NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL,
	active boolean NULL DEFAULT true
);
ALTER TABLE component.group_component ADD CONSTRAINT PK_group_component
	PRIMARY KEY (id);
ALTER TABLE component.group_component ADD CONSTRAINT FK_group_component_user
	FOREIGN KEY (owner_user_id) REFERENCES usr.user (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.group_component ADD CONSTRAINT FK_group_component_component_visibility
	FOREIGN KEY (visibility_id) REFERENCES component.component_visibility (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.source_descriptor
DROP TABLE IF EXISTS component.source_descriptor CASCADE;
CREATE TABLE component.source_descriptor
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
ALTER TABLE component.source_descriptor ADD CONSTRAINT PK_source_descriptor PRIMARY KEY (id);
ALTER TABLE component.source_descriptor ADD CONSTRAINT FK_data_descriptor_data_format
	FOREIGN KEY (data_format_id) REFERENCES product.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.source_descriptor ADD CONSTRAINT FK_data_descriptor_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES product.sensor_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.target_descriptor
DROP TABLE IF EXISTS component.target_descriptor CASCADE;
CREATE TABLE component.target_descriptor
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
ALTER TABLE component.target_descriptor ADD CONSTRAINT PK_target_descriptor PRIMARY KEY (id);
ALTER TABLE component.target_descriptor ADD CONSTRAINT FK_data_descriptor_data_format
	FOREIGN KEY (data_format_id) REFERENCES product.data_format (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.target_descriptor ADD CONSTRAINT FK_data_descriptor_sensor_type
	FOREIGN KEY (sensor_type_id) REFERENCES product.sensor_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.processing_component_sources
DROP TABLE IF EXISTS component.processing_component_sources CASCADE;
CREATE TABLE component.processing_component_sources
(
	processing_component_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.processing_component_sources ADD CONSTRAINT PK_processing_component_sources
	PRIMARY KEY (processing_component_id, source_descriptor_id);
ALTER TABLE component.processing_component_sources ADD CONSTRAINT FK_processing_component_sources_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES component.processing_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_component_sources ADD CONSTRAINT FK_processing_component_sources_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES component.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.processing_component_targets
DROP TABLE IF EXISTS component.processing_component_targets CASCADE;
CREATE TABLE component.processing_component_targets
(
	processing_component_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.processing_component_targets ADD CONSTRAINT PK_processing_component_targets
	PRIMARY KEY (processing_component_id, target_descriptor_id);
ALTER TABLE component.processing_component_targets ADD CONSTRAINT FK_processing_component_targets_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES component.processing_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.processing_component_targets ADD CONSTRAINT FK_processing_component_targets_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES component.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.group_component_sources
DROP TABLE IF EXISTS component.group_component_sources CASCADE;
CREATE TABLE component.group_component_sources
(
	group_component_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.group_component_sources ADD CONSTRAINT PK_group_component_sources
	PRIMARY KEY (group_component_id, source_descriptor_id);
ALTER TABLE component.group_component_sources ADD CONSTRAINT FK_group_component_sources_group_component
	FOREIGN KEY (group_component_id) REFERENCES component.group_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.group_component_sources ADD CONSTRAINT FK_group_component_sources_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES component.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.group_component_targets
DROP TABLE IF EXISTS component.group_component_targets CASCADE;
CREATE TABLE component.group_component_targets
(
	group_component_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.group_component_targets ADD CONSTRAINT PK_group_component_targets
	PRIMARY KEY (group_component_id, target_descriptor_id);
ALTER TABLE component.group_component_targets ADD CONSTRAINT FK_group_component_targets_group_component
	FOREIGN KEY (group_component_id) REFERENCES component.group_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.group_component_targets ADD CONSTRAINT FK_group_component_targets_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES component.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.data_source_component
DROP TABLE IF EXISTS component.data_source_component CASCADE;
CREATE TABLE component.data_source_component
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
ALTER TABLE component.data_source_component ADD CONSTRAINT PK_data_source_component
	PRIMARY KEY (id);
ALTER TABLE component.data_source_component ADD CONSTRAINT FK_data_source_component_fetch_mode
	FOREIGN KEY (fetch_mode_id) REFERENCES component.fetch_mode (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.data_source_component_sources
DROP TABLE IF EXISTS component.data_source_component_sources CASCADE;
CREATE TABLE component.data_source_component_sources
(
	data_source_component_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.data_source_component_sources ADD CONSTRAINT PK_data_source_component_sources
	PRIMARY KEY (data_source_component_id, source_descriptor_id);
ALTER TABLE component.data_source_component_sources ADD CONSTRAINT FK_data_source_component_sources_data_source_component
	FOREIGN KEY (data_source_component_id) REFERENCES component.data_source_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.data_source_component_sources ADD CONSTRAINT FK_data_source_component_sources_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES component.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.data_source_component_targets
DROP TABLE IF EXISTS component.data_source_component_targets CASCADE;
CREATE TABLE component.data_source_component_targets
(
	data_source_component_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.data_source_component_targets ADD CONSTRAINT PK_data_source_component_targets
	PRIMARY KEY (data_source_component_id, target_descriptor_id);
ALTER TABLE component.data_source_component_targets ADD CONSTRAINT FK_data_source_component_targets_data_source_component
	FOREIGN KEY (data_source_component_id) REFERENCES component.data_source_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.data_source_component_targets ADD CONSTRAINT FK_data_source_component_targets_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES component.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.data_source_component_group
DROP TABLE IF EXISTS component.data_source_component_group CASCADE;
CREATE TABLE component.data_source_component_group
(
	id varchar(512) NOT NULL,
	label varchar(250) NOT NULL,
	version varchar(50) NOT NULL,
	description text NOT NULL,
	authors varchar(1024) NOT NULL,
	copyright text NOT NULL,
	node_affinity varchar(250) NULL,
	tags text NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL DEFAULT now()
);
ALTER TABLE component.data_source_component_group ADD CONSTRAINT PK_data_source_component_group
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: component.data_source_component_group_sources
DROP TABLE IF EXISTS component.data_source_component_group_sources CASCADE;
CREATE TABLE component.data_source_component_group_sources
(
	data_source_component_group_id varchar(512) NOT NULL,
	source_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.data_source_component_group_sources ADD CONSTRAINT PK_data_source_component_group_sources
	PRIMARY KEY (data_source_component_group_id, source_descriptor_id);
ALTER TABLE component.data_source_component_group_sources ADD CONSTRAINT FK_data_source_component_group_sources_1
	FOREIGN KEY (data_source_component_group_id) REFERENCES component.data_source_component_group (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.data_source_component_group_sources ADD CONSTRAINT FK_data_source_component_group_sources_2
	FOREIGN KEY (source_descriptor_id) REFERENCES component.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.data_source_component_group_targets
DROP TABLE IF EXISTS component.data_source_component_group_targets CASCADE;
CREATE TABLE component.data_source_component_group_targets
(
	data_source_component_group_id varchar(512) NOT NULL,
	target_descriptor_id varchar(512) NOT NULL
);
ALTER TABLE component.data_source_component_group_targets ADD CONSTRAINT PK_data_source_component_group_targets
	PRIMARY KEY (data_source_component_group_id, target_descriptor_id);
ALTER TABLE component.data_source_component_group_targets ADD CONSTRAINT FK_data_source_component_group_targets_1
	FOREIGN KEY (data_source_component_group_id) REFERENCES component.data_source_component_group (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.data_source_component_group_targets ADD CONSTRAINT FK_data_source_component_group_targets_2
	FOREIGN KEY (target_descriptor_id) REFERENCES component.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component.data_source_component_group_components
DROP TABLE IF EXISTS component.data_source_component_group_components CASCADE;
CREATE TABLE component.data_source_component_group_components
(
	data_source_component_group_id varchar(512) NOT NULL,
	data_source_component_id varchar(512) NOT NULL
);
ALTER TABLE component.data_source_component_group_components ADD CONSTRAINT PK_data_source_component_group_components
	PRIMARY KEY (data_source_component_group_id, data_source_component_id);
ALTER TABLE component.data_source_component_group_components ADD CONSTRAINT FK_data_source_component_group_components_1
	FOREIGN KEY (data_source_component_group_id) REFERENCES component.data_source_component_group (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE component.data_source_component_group_components ADD CONSTRAINT FK_data_source_component_group_components_2
	FOREIGN KEY (data_source_component_id) REFERENCES component.data_source_component (id) ON DELETE No Action ON UPDATE No Action;