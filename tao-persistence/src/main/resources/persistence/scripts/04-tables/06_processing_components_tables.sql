-------------------------------------------------------------------------------
-- table: parameter_type
DROP TABLE IF EXISTS tao.parameter_type CASCADE;
CREATE TABLE tao.parameter_type
(
	id integer NOT NULL,
	type varchar(250) NOT NULL
);
ALTER TABLE tao.parameter_type ADD CONSTRAINT PK_parameter_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: component_visibility
DROP TABLE IF EXISTS tao.component_visibility CASCADE;
CREATE TABLE tao.component_visibility
(
	id integer NOT NULL,
	visibility varchar(50) NOT NULL
);
ALTER TABLE tao.component_visibility ADD CONSTRAINT PK_component_visibility
	PRIMARY KEY (id);
-------------------------------------------------------------------------------
-- table: processing_component_type
DROP TABLE IF EXISTS tao.processing_component_type CASCADE;
CREATE TABLE tao.processing_component_type
(
	id integer NOT NULL,
	type varchar(50) NOT NULL
);
ALTER TABLE tao.processing_component_type ADD CONSTRAINT PK_processing_component_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: template_type
DROP TABLE IF EXISTS tao.template_type CASCADE;
CREATE TABLE tao.template_type
(
	id integer NOT NULL,
	type varchar(250) NOT NULL
);
ALTER TABLE tao.template_type ADD CONSTRAINT PK_template_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: container
DROP TABLE IF EXISTS tao.container CASCADE;
CREATE TABLE tao.container
(
	id varchar(1024) NOT NULL,
	name varchar(1024) NOT NULL,
	tag varchar(1024) NOT NULL,
	application_path varchar(1024) NULL,
	logo_image varchar NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL
);
ALTER TABLE tao.container ADD CONSTRAINT PK_container
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: container_applications
DROP TABLE IF EXISTS tao.container_applications CASCADE;
CREATE TABLE tao.container_applications
(
	container_id varchar(1024) NOT NULL,
	name varchar(1024) NOT NULL,
	path varchar(1024) NULL,
	parallel_flag varchar(50) NULL,
	memory integer NULL
);
ALTER TABLE tao.container_applications ADD CONSTRAINT PK_container_applications
	PRIMARY KEY (container_id, name);
ALTER TABLE tao.container_applications ADD CONSTRAINT FK_container_applications_container
	FOREIGN KEY (container_id) REFERENCES tao.container (id) ON DELETE No Action ON UPDATE No Action;
-------------------------------------------------------------------------------
-- table: tag_type
DROP TABLE IF EXISTS tao.tag_type CASCADE;
CREATE TABLE tao.tag_type
(
	id integer NOT NULL,
	description varchar(20) NOT NULL
);
ALTER TABLE tao.tag_type ADD CONSTRAINT PK_tag_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tag
DROP TABLE IF EXISTS tao.tag CASCADE;
CREATE TABLE tao.tag
(
	id bigserial NOT NULL,
	tag_type_id integer NOT NULL,
	tag text NOT NULL
);
ALTER TABLE tao.tag ADD CONSTRAINT PK_tag
	PRIMARY KEY (id);
ALTER TABLE tao.tag ADD CONSTRAINT FK_tag_tag_type
	FOREIGN KEY (tag_type_id) REFERENCES tao.tag_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: processing_component
DROP TABLE IF EXISTS tao.processing_component CASCADE;
CREATE TABLE tao.processing_component
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
ALTER TABLE tao.processing_component ADD CONSTRAINT PK_processing_component
	PRIMARY KEY (id);
ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_template_type
	FOREIGN KEY (template_type_id) REFERENCES tao.template_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_user
	FOREIGN KEY (owner_user) REFERENCES tao.user (username) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_component_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.component_visibility (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_processing_component_type
	FOREIGN KEY (component_type_id) REFERENCES tao.processing_component_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_container
	FOREIGN KEY (container_id) REFERENCES tao.container (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: processing_parameter
DROP TABLE IF EXISTS tao.processing_parameter CASCADE;
CREATE TABLE tao.processing_parameter
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
ALTER TABLE tao.processing_parameter ADD CONSTRAINT PK_processing_parameter
	PRIMARY KEY (id);
ALTER TABLE tao.processing_parameter ADD CONSTRAINT FK_processing_parameter_parameter_type
	FOREIGN KEY (type_id) REFERENCES tao.parameter_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.condition
DROP TABLE IF EXISTS tao.condition CASCADE;
CREATE TABLE tao.condition
(
    id integer,
    description varchar(250)
);
ALTER TABLE tao.condition ADD CONSTRAINT PK_condition
    PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: processing_parameter_dependencies
DROP TABLE IF EXISTS tao.processing_parameter_dependencies CASCADE;
CREATE TABLE tao.processing_parameter_dependencies
(
    parameter_id varchar(512) NOT NULL,
    dependant_parameter_id varchar(512) NOT NULL,
    dependency_condition_id integer NOT NULL,
    dependant_parameter_value varchar(250) NULL
);
ALTER TABLE tao.processing_parameter_dependencies ADD CONSTRAINT PK_processing_parameter_dependencies
    PRIMARY KEY (parameter_id, dependant_parameter_id);
ALTER TABLE tao.processing_parameter_dependencies ADD CONSTRAINT FK_processing_parameter_dependencies_processing_parameter_1
    FOREIGN KEY (parameter_id) REFERENCES tao.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_parameter_dependencies ADD CONSTRAINT FK_processing_parameter_dependencies_processing_parameter_2
    FOREIGN KEY (dependant_parameter_id) REFERENCES tao.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.processing_parameter_dependencies ADD CONSTRAINT FK_processing_parameter_dependencies_condition
    FOREIGN KEY (dependency_condition_id) REFERENCES tao.condition (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component_parameters
DROP TABLE IF EXISTS tao.component_parameters CASCADE;
CREATE TABLE tao.component_parameters
(
	processing_component_id varchar(512) NOT NULL,
	processing_parameter_id varchar(512) NOT NULL
);
ALTER TABLE tao.component_parameters ADD CONSTRAINT PK_component_parameters
	PRIMARY KEY (processing_component_id, processing_parameter_id);
ALTER TABLE tao.component_parameters ADD CONSTRAINT FK_component_parameters_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.component_parameters ADD CONSTRAINT FK_component_parameters_processing_parameter
	FOREIGN KEY (processing_parameter_id) REFERENCES tao.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: template_parameter_parameters
DROP TABLE IF EXISTS tao.template_parameter_parameters CASCADE;
CREATE TABLE tao.template_parameter_parameters
(
    template_parameter_id varchar(512) NOT NULL,
	regular_parameter_id varchar(512) NOT NULL
);
ALTER TABLE tao.template_parameter_parameters ADD CONSTRAINT PK_template_parameter_parameters
	PRIMARY KEY (template_parameter_id, regular_parameter_id);
ALTER TABLE tao.template_parameter_parameters ADD CONSTRAINT FK_template_parameter_parameters_processing_parameter_01
	FOREIGN KEY (template_parameter_id) REFERENCES tao.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.template_parameter_parameters ADD CONSTRAINT FK_template_parameter_parameters_processing_parameter_02
	FOREIGN KEY (regular_parameter_id) REFERENCES tao.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component_parameter_values_set
DROP TABLE IF EXISTS tao.component_parameter_values_set CASCADE;
CREATE TABLE tao.component_parameter_values_set
(
	parameter_id varchar(512) NOT NULL,
	processing_component_id varchar(512) NOT NULL,
	parameter_value varchar(250) NOT NULL
);
ALTER TABLE tao.component_parameter_values_set ADD CONSTRAINT PK_component_parameter_values_set
	PRIMARY KEY (parameter_id, processing_component_id, parameter_value);
ALTER TABLE tao.component_parameter_values_set ADD CONSTRAINT FK_component_parameter_values_set_processing_parameter
	FOREIGN KEY (parameter_id) REFERENCES tao.processing_parameter (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.component_parameter_values_set ADD CONSTRAINT FK_component_parameter_values_set_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: component_variables
DROP TABLE IF EXISTS tao.component_variables CASCADE;
CREATE TABLE tao.component_variables
(
	processing_component_id varchar(512) NOT NULL,
	key varchar(512) NOT NULL,
	value varchar(512) NOT NULL
);
ALTER TABLE tao.component_variables ADD CONSTRAINT PK_component_variables
	PRIMARY KEY (processing_component_id, key);
ALTER TABLE tao.component_variables ADD CONSTRAINT FK_component_variables_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;
	
-------------------------------------------------------------------------------
-- table: group_component
DROP TABLE IF EXISTS tao.group_component CASCADE;
CREATE TABLE tao.group_component
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
ALTER TABLE tao.group_component ADD CONSTRAINT PK_group_component
	PRIMARY KEY (id);
ALTER TABLE tao.group_component ADD CONSTRAINT FK_group_component_user
	FOREIGN KEY (owner_user_id) REFERENCES tao.user (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.group_component ADD CONSTRAINT FK_group_component_component_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.component_visibility (id) ON DELETE No Action ON UPDATE No Action;