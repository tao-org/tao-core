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
-- table: parameter
DROP TABLE IF EXISTS tao.parameter CASCADE;

CREATE TABLE tao.parameter
(
	id bigint NOT NULL,
	name varchar(50) NOT NULL,
	data_type varchar(50) NOT NULL,
	default_value varchar(250) NULL,
	description varchar(250) NULL,
	not_null boolean NULL,
	not_empty boolean NULL,
	type_id integer NULL
);

ALTER TABLE tao.parameter ADD CONSTRAINT PK_parameter
	PRIMARY KEY (id);
	
ALTER TABLE tao.parameter ADD CONSTRAINT FK_parameter_parameter_type
	FOREIGN KEY (type_id) REFERENCES tao.parameter_type (id) ON DELETE No Action ON UPDATE No Action;
	

-------------------------------------------------------------------------------
-- table: parameter_parameter
DROP TABLE IF EXISTS tao.parameter_parameter CASCADE;

CREATE TABLE tao.parameter_parameter
(
	template_parameter_id bigint NOT NULL,
	regular_parameter_id bigint NOT NULL
);

ALTER TABLE tao.parameter_parameter ADD CONSTRAINT PK_parameter_parameter
	PRIMARY KEY (template_parameter_id, regular_parameter_id);
	
ALTER TABLE tao.parameter_parameter ADD CONSTRAINT FK_parameter_parameter_parameter_01
	FOREIGN KEY (template_parameter_id) REFERENCES tao.parameter (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.parameter_parameter ADD CONSTRAINT FK_parameter_parameter_parameter_02
	FOREIGN KEY (regular_parameter_id) REFERENCES tao.parameter (id) ON DELETE No Action ON UPDATE No Action;
	
	
-------------------------------------------------------------------------------
-- table: parameter_values_set
DROP TABLE IF EXISTS tao.parameter_values_set CASCADE;

CREATE TABLE tao.parameter_values_set
(
	parameter_id bigint NOT NULL,
	parameter_value varchar(250) NOT NULL
);

ALTER TABLE tao.parameter_values_set ADD CONSTRAINT PK_parameter_values_set
	PRIMARY KEY (parameter_id, parameter_value);

ALTER TABLE tao.parameter_values_set ADD CONSTRAINT FK_parameter_values_set_parameter
	FOREIGN KEY (parameter_id) REFERENCES tao.parameter (id) ON DELETE No Action ON UPDATE No Action;
	
	
-------------------------------------------------------------------------------
-- table: operation_source
DROP TABLE IF EXISTS tao.operation_source CASCADE;

CREATE TABLE tao.operation_source
(
	id integer NOT NULL,
	source varchar(50) NOT NULL
);

ALTER TABLE tao.operation_source ADD CONSTRAINT PK_operation_source
	PRIMARY KEY (id);	


-------------------------------------------------------------------------------
-- table: processing_operation
DROP TABLE IF EXISTS tao.processing_operation CASCADE;

CREATE TABLE tao.processing_operation
(
	id integer NOT NULL,
	name varchar(250) NULL,
	progress_pattern varchar(500) NULL,
	error_pattern varchar(500) NULL,
	source_id integer NOT NULL,
	is_handling_output_name boolean NULL
);

ALTER TABLE tao.processing_operation ADD CONSTRAINT PK_processing_operation
	PRIMARY KEY (id);

ALTER TABLE tao.processing_operation ADD CONSTRAINT FK_processing_operation_operation_source
	FOREIGN KEY (source_id) REFERENCES tao.operation_source (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: operation_parameters
DROP TABLE IF EXISTS tao.operation_parameters CASCADE;

CREATE TABLE tao.operation_parameters
(
	operation_id integer NOT NULL,
	parameter_id bigint NULL
);

ALTER TABLE tao.operation_parameters ADD CONSTRAINT PK_operation_parameters
	PRIMARY KEY (operation_id, parameter_id);
	
ALTER TABLE tao.operation_parameters ADD CONSTRAINT FK_operation_parameters_parameter
	FOREIGN KEY (parameter_id) REFERENCES tao.parameter (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.operation_parameters ADD CONSTRAINT FK_operation_parameters_processing_operation
	FOREIGN KEY (operation_id) REFERENCES tao.processing_operation (id) ON DELETE No Action ON UPDATE No Action;

	
-------------------------------------------------------------------------------
-- table: operation_variables
DROP TABLE IF EXISTS tao.operation_variables CASCADE;

CREATE TABLE tao.operation_variables
(
	operation_id integer NOT NULL,
	variable_name varchar(50) NOT NULL,
	variable_value varchar(500) NOT NULL
);

ALTER TABLE tao.operation_variables ADD CONSTRAINT PK_operation_variables
	PRIMARY KEY (operation_id, variable_name);
	
ALTER TABLE tao.operation_variables ADD CONSTRAINT FK_operation_variables_processing_operation
	FOREIGN KEY (operation_id) REFERENCES tao.processing_operation (id) ON DELETE No Action ON UPDATE No Action;
	
	
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
	id integer NOT NULL,
	name varchar(250) NOT NULL,
	label varchar(250) NOT NULL,
	version varchar(50) NOT NULL,
	description varchar(1024) NOT NULL,
	authors varchar(50) NULL,
	copyright text NOT NULL,
	main_tool_file_location varchar(512) NULL,
	working_directory varchar(512) NULL,
	template_type_id integer NOT NULL,
	template_name varchar(500) NOT NULL,
	owner_user_id integer NULL,
	visibility_id integer NOT NULL,
	operation_id integer NOT NULL,
	multi_thread boolean NOT NULL,
	created timestamp NOT NULL,
    modified timestamp NULL,
	active boolean NULL
);

ALTER TABLE tao.processing_component ADD CONSTRAINT PK_processing_component
	PRIMARY KEY (id);

ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_template_type
	FOREIGN KEY (template_type_id) REFERENCES tao.template_type (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_user
	FOREIGN KEY (owner_user_id) REFERENCES tao.user (id) ON DELETE No Action ON UPDATE No Action;
	
ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_component_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.component_visibility (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.processing_component ADD CONSTRAINT FK_processing_component_processing_operation
	FOREIGN KEY (operation_id) REFERENCES tao.processing_operation (id) ON DELETE No Action ON UPDATE No Action;

