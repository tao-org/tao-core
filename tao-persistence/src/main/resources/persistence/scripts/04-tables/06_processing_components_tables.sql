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
-- table: component_source
DROP TABLE IF EXISTS tao.component_source CASCADE;

CREATE TABLE tao.component_source
(
	id integer NOT NULL,
	source varchar(50) NOT NULL
);

ALTER TABLE tao.component_source ADD CONSTRAINT PK_component_source
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
-- table: processing_component
DROP TABLE IF EXISTS tao.processing_component CASCADE;

CREATE TABLE tao.processing_component
(
	id varchar(512) NOT NULL,
	label varchar(250) NOT NULL,
	version varchar(50) NOT NULL,
	description text NOT NULL,
	authors varchar(1024) NOT NULL,
	copyright text NOT NULL,
	main_tool_file_location varchar(512) NOT NULL,
	working_directory varchar(512) NULL,
	template_type_id integer NOT NULL,
	template_name varchar(512) NOT NULL,
	owner_user_id integer NULL,
	visibility_id integer NOT NULL,
	multi_thread boolean NOT NULL DEFAULT false,
	node_affinity varchar(250) NULL,
	created timestamp NOT NULL DEFAULT now(),
    modified timestamp NULL,
	active boolean NOT NULL DEFAULT true
);

ALTER TABLE tao.processing_component ADD CONSTRAINT PK_processing_component
	PRIMARY KEY (id);

ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_template_type
	FOREIGN KEY (template_type_id) REFERENCES tao.template_type (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_user
	FOREIGN KEY (owner_user_id) REFERENCES tao.user (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_component_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.component_visibility (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: component_parameters
DROP TABLE IF EXISTS tao.component_parameters CASCADE;

CREATE TABLE tao.component_parameters
(
	id varchar(512) NOT NULL,
    type_id integer NOT NULL,
    label varchar(512) NOT NULL,
    data_type varchar(512) NOT NULL,
    default_value varchar(250) NULL,
    description text NULL,
    unit varchar(250) NULL,
    format varchar(250) NULL,
    value_set text NULL,
    not_null boolean NULL,
    not_empty boolean NULL,
    processing_component_id varchar(512) NOT NULL
);

ALTER TABLE tao.component_parameters ADD CONSTRAINT PK_component_parameters
	PRIMARY KEY (id, processing_component_id);

ALTER TABLE tao.component_parameters ADD CONSTRAINT FK_component_parameters_parameter_type
	FOREIGN KEY (type_id) REFERENCES tao.parameter_type (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.component_parameters ADD CONSTRAINT FK_component_parameters_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: template_parameter_parameters
DROP TABLE IF EXISTS tao.template_parameter_parameters CASCADE;

CREATE TABLE tao.template_parameter_parameters
(
    processing_component_id varchar(512) NOT NULL,
	template_parameter_id varchar(512) NOT NULL,
	regular_parameter_id varchar(512) NOT NULL
);

ALTER TABLE tao.template_parameter_parameters ADD CONSTRAINT PK_template_parameter_parameters
	PRIMARY KEY (processing_component_id, template_parameter_id, regular_parameter_id);

--ALTER TABLE tao.template_parameter_parameters ADD CONSTRAINT FK_template_parameter_parameters_processing_component
--	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.template_parameter_parameters ADD CONSTRAINT FK_template_parameter_parameters_parameter_01
	FOREIGN KEY (template_parameter_id, processing_component_id) REFERENCES tao.component_parameters (id, processing_component_id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.template_parameter_parameters ADD CONSTRAINT FK_template_parameter_parameters_parameter_02
	FOREIGN KEY (regular_parameter_id, processing_component_id) REFERENCES tao.component_parameters (id, processing_component_id) ON DELETE No Action ON UPDATE No Action;


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

ALTER TABLE tao.component_parameter_values_set ADD CONSTRAINT FK_component_parameter_values_set_component_parameters
	FOREIGN KEY (parameter_id, processing_component_id) REFERENCES tao.component_parameters (id, processing_component_id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: component_variables
DROP TABLE IF EXISTS tao.component_variables CASCADE;

CREATE TABLE tao.component_variables
(
	id varchar(512) NOT NULL,
	key varchar(512) NOT NULL,
	value varchar(512) NOT NULL
);

ALTER TABLE tao.component_variables ADD CONSTRAINT PK_component_variables
	PRIMARY KEY (id, key);
	
ALTER TABLE tao.component_variables ADD CONSTRAINT FK_component_variables_processing_component
	FOREIGN KEY (id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;
	

