-------------------------------------------------------------------------------
-- table: query
DROP TABLE IF EXISTS tao.query CASCADE;

CREATE TABLE tao.query
(
	id varchar(512) NOT NULL,
	sensor_name varchar(1024) NOT NULL,
    data_source_name  varchar(512) NOT NULL,
	username varchar(512) NULL,
	password bytea NULL,
	page_size integer NULL,
	page_number integer NULL,
	_limit integer NULL,  -- reserved word
	"values" json NULL,    -- reserved word
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL
);

ALTER TABLE tao.query ADD CONSTRAINT PK_query
	PRIMARY KEY (id);



-------------------------------------------------------------------------------
-- table: workflow_graph_status
DROP TABLE IF EXISTS tao.workflow_graph_status CASCADE;

CREATE TABLE tao.workflow_graph_status
(
	id integer NOT NULL,
	status varchar(250) NOT NULL
);

ALTER TABLE tao.workflow_graph_status ADD CONSTRAINT PK_workflow_graph_status PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: workflow_graph_visibility
DROP TABLE IF EXISTS tao.workflow_graph_visibility CASCADE;

CREATE TABLE tao.workflow_graph_visibility
(
	id integer NOT NULL,
	visibility varchar(250) NOT NULL
);

ALTER TABLE tao.workflow_graph_visibility ADD CONSTRAINT PK_workflow_graph_visibility PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: workflow_graph
DROP TABLE IF EXISTS tao.workflow_graph CASCADE;

CREATE TABLE tao.workflow_graph
(
	id bigint NOT NULL,
	name varchar(250) NOT NULL,
	created timestamp NULL DEFAULT now(),
	username varchar(50) NOT NULL,
	definition_path varchar(512) NULL,
	status_id integer NOT NULL,
	visibility_id integer NOT NULL,
	custom_values json NULL,
	active boolean NULL DEFAULT true
);

ALTER TABLE tao.workflow_graph ADD CONSTRAINT PK_workflow PRIMARY KEY (id);

ALTER TABLE tao.workflow_graph ADD CONSTRAINT FK_workflow_graph_status
	FOREIGN KEY (status_id) REFERENCES tao.workflow_graph_status (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.workflow_graph ADD CONSTRAINT FK_workflow_graph_visibility
	FOREIGN KEY (visibility_id) REFERENCES tao.workflow_graph_visibility (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.workflow_graph ADD CONSTRAINT FK_workflow_user
	FOREIGN KEY (username) REFERENCES tao."user" (username) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: workflow_graph_processing_custom_values
--DROP TABLE IF EXISTS tao.workflow_graph_processing_custom_values CASCADE;

--CREATE TABLE tao.workflow_graph_processing_custom_values
--(
--	workflow_graph_id bigint NOT NULL,
--	name varchar(512) NOT NULL,
--	value text NOT NULL
--);

--ALTER TABLE tao.workflow_graph_processing_custom_values ADD CONSTRAINT PK_workflow_graph_processing_custom_values
--	PRIMARY KEY (workflow_graph_id, name);

--ALTER TABLE tao.workflow_graph_processing_custom_values ADD CONSTRAINT FK_workflow_graph_processing_custom_values_workflow_graph
--	FOREIGN KEY (workflow_graph_id) REFERENCES tao.workflow_graph (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: graph_node
DROP TABLE IF EXISTS tao.graph_node CASCADE;

CREATE TABLE tao.graph_node
(
	id bigint NOT NULL,
	name varchar(250) NULL,
	created timestamp NULL DEFAULT now(),
	workflow_id bigint NOT NULL,
	processing_component_id varchar(512) NOT NULL,
	xCoord real NULL,
	yCoord real NULL,
	custom_values json NULL,
    -- special column used by JPA to distinguish which type of object is stored in one row (since this table holds 2 types of entities)
    discriminator integer NOT NULL
);

ALTER TABLE tao.graph_node ADD CONSTRAINT PK_graph_node PRIMARY KEY (id);

ALTER TABLE tao.graph_node ADD CONSTRAINT FK_graph_node_workflow_graph
	FOREIGN KEY (workflow_id) REFERENCES tao.workflow_graph (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.graph_node ADD CONSTRAINT FK_graph_node_processing_component
	FOREIGN KEY (processing_component_id) REFERENCES tao.processing_component (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: graph_node_processing_custom_values
--DROP TABLE IF EXISTS tao.graph_node_processing_custom_values CASCADE;

--CREATE TABLE tao.graph_node_processing_custom_values
--(
--	graph_node_id bigint NOT NULL,
--	name varchar(512) NOT NULL,
--	value text NOT NULL
--);

--ALTER TABLE tao.graph_node_processing_custom_values ADD CONSTRAINT PK_graph_node_processing_custom_values
--	PRIMARY KEY (graph_node_id, name);

--ALTER TABLE tao.graph_node_processing_custom_values ADD CONSTRAINT FK_graph_node_processing_custom_values_graph_node
--	FOREIGN KEY (graph_node_id) REFERENCES tao.graph_node (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: graph_node_group_nodes
DROP TABLE IF EXISTS tao.graph_node_group_nodes CASCADE;

CREATE TABLE tao.graph_node_group_nodes
(
    graph_node_group_id bigint NOT NULL,
	graph_node_id bigint NOT NULL
);

ALTER TABLE tao.graph_node_group_nodes ADD CONSTRAINT PK_graph_node_group_nodes
	PRIMARY KEY (graph_node_group_id, graph_node_id);

ALTER TABLE tao.graph_node_group_nodes ADD CONSTRAINT FK_graph_node_group_nodes_graph_node_01
	FOREIGN KEY (graph_node_group_id) REFERENCES tao.graph_node (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.graph_node_group_nodes ADD CONSTRAINT FK_graph_node_group_nodes_graph_node_02
	FOREIGN KEY (graph_node_id) REFERENCES tao.graph_node (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- execution_status
DROP TABLE IF EXISTS tao.execution_status CASCADE;

CREATE TABLE tao.execution_status
(
	id integer NOT NULL,
	status varchar(50) NOT NULL
);

ALTER TABLE tao.execution_status ADD CONSTRAINT PK_execution_status PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: job
DROP TABLE IF EXISTS tao.job CASCADE;

CREATE TABLE tao.job
(
	id bigint NOT NULL,
	start_time timestamp without time zone NULL,
	end_time timestamp without time zone NULL,
	workflow_id bigint NOT NULL,
	username varchar(50) NOT NULL,
	query_id varchar(512) NULL,
	execution_status_id integer NOT NULL
);

ALTER TABLE tao.job ADD CONSTRAINT PK_job PRIMARY KEY (id);

ALTER TABLE tao.job ADD CONSTRAINT FK_job_workflow
	FOREIGN KEY (workflow_id) REFERENCES tao.workflow_graph (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.job ADD CONSTRAINT FK_job_user
	FOREIGN KEY (username) REFERENCES tao."user" (username) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.job ADD CONSTRAINT FK_job_query
	FOREIGN KEY (query_id) REFERENCES tao.query (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.job ADD CONSTRAINT FK_job_execution_status
	FOREIGN KEY (execution_status_id) REFERENCES tao.execution_status (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: execution_node
DROP TABLE IF EXISTS tao.execution_node CASCADE;

CREATE TABLE tao.execution_node
(
	host_name varchar(250) NOT NULL,
	username varchar(50) NOT NULL,
	password text NOT NULL,
	total_CPU integer NOT NULL,
	total_RAM integer NOT NULL,
	total_HDD integer NOT NULL,
	description text NULL,
	SSH_key text NULL,
	used_CPU integer NULL,
	used_RAM integer NULL,
	used_HDD integer NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL,
	active boolean NULL DEFAULT true
);

ALTER TABLE tao.execution_node ADD CONSTRAINT PK_execution_node PRIMARY KEY (host_name);


-------------------------------------------------------------------------------
-- table: task
DROP TABLE IF EXISTS tao.task CASCADE;

CREATE TABLE tao.task
(
	id bigint NOT NULL,
	component_id varchar(512) NOT NULL, -- FK to either tao.processing_component, either tao.data_source_component
	resource_id varchar(512) NULL,
	graph_node_id bigint NOT NULL,
	start_time timestamp without time zone NULL,
	end_time timestamp without time zone NULL,
	internal_state varchar(512) NULL,
	job_id bigint NOT NULL,
	task_group_id bigint NULL,
	execution_node_host_name varchar(250) NULL,
	execution_status_id integer NOT NULL,
	used_CPU integer NULL,
    used_RAM integer NULL,
    used_HDD integer NULL,
    -- special column used by JPA to distinguish which type of object is stored in one row (since this table holds 2 types of entities)
    discriminator integer NOT NULL
);

ALTER TABLE tao.task ADD CONSTRAINT PK_task PRIMARY KEY (id);

ALTER TABLE tao.task ADD CONSTRAINT FK_task_graph_node
	FOREIGN KEY (graph_node_id) REFERENCES tao.graph_node (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.task ADD CONSTRAINT FK_task_job
	FOREIGN KEY (job_id) REFERENCES tao.job (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.task ADD CONSTRAINT FK_task_group
	FOREIGN KEY (task_group_id) REFERENCES tao.task (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.task ADD CONSTRAINT FK_task_execution_node
	FOREIGN KEY (execution_node_host_name) REFERENCES tao.execution_node (host_name) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.task ADD CONSTRAINT FK_task_execution_status
	FOREIGN KEY (execution_status_id) REFERENCES tao.execution_status (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: execution_group_tasks
DROP TABLE IF EXISTS tao.execution_group_tasks CASCADE;

CREATE TABLE tao.execution_group_tasks
(
    execution_group_id bigint NOT NULL,
	execution_task_id bigint NOT NULL
);

ALTER TABLE tao.execution_group_tasks ADD CONSTRAINT PK_execution_group_tasks
	PRIMARY KEY (execution_group_id, execution_task_id);

ALTER TABLE tao.execution_group_tasks ADD CONSTRAINT FK_execution_group_tasks_execution_group
	FOREIGN KEY (execution_group_id) REFERENCES tao.task (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.execution_group_tasks ADD CONSTRAINT FK_execution_group_tasks_execution_task
	FOREIGN KEY (execution_task_id) REFERENCES tao.task (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: task_inputs
DROP TABLE IF EXISTS tao.task_inputs CASCADE;

CREATE TABLE tao.task_inputs
(
	task_id bigint NOT NULL,
	key varchar(512) NOT NULL,
	value varchar(512) NOT NULL
);

ALTER TABLE tao.task_inputs ADD CONSTRAINT PK_task_inputs PRIMARY KEY (task_id, key);

ALTER TABLE tao.task_inputs ADD CONSTRAINT FK_task_inputs_task
	FOREIGN KEY (task_id) REFERENCES tao.task (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: task_output
DROP TABLE IF EXISTS tao.task_output CASCADE;

CREATE TABLE tao.task_output
(
	task_id bigint NOT NULL,
	key varchar(512) NOT NULL,
    value varchar(512) NOT NULL
);

ALTER TABLE tao.task_output ADD CONSTRAINT PK_task_output PRIMARY KEY (task_id, key);

ALTER TABLE tao.task_output ADD CONSTRAINT FK_task_output_task
	FOREIGN KEY (task_id) REFERENCES tao.task (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- service_status
DROP TABLE IF EXISTS tao.service_status CASCADE;

CREATE TABLE tao.service_status
(
	id integer NOT NULL,
	status varchar(50) NOT NULL
);

ALTER TABLE tao.service_status ADD CONSTRAINT PK_service_status PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: service
DROP TABLE IF EXISTS tao.service CASCADE;

CREATE TABLE tao.service
(
    id integer NOT NULL,
	name varchar(250) NOT NULL,
	version varchar(50) NOT NULL,
	description text NULL
);

ALTER TABLE tao.service ADD CONSTRAINT PK_service PRIMARY KEY (id);

ALTER TABLE tao.service ADD CONSTRAINT K_service UNIQUE (name, version);


-------------------------------------------------------------------------------
-- table: execution_node_service
DROP TABLE IF EXISTS tao.execution_node_service CASCADE;

CREATE TABLE tao.execution_node_service
(
	host_name varchar(250) NOT NULL,
	service_id integer NOT NULL,
	service_status_id integer NOT NULL
);

ALTER TABLE tao.execution_node_service ADD CONSTRAINT PK_execution_node_service PRIMARY KEY (host_name, service_id);

ALTER TABLE tao.execution_node_service ADD CONSTRAINT FK_execution_node_service_execution_node
	FOREIGN KEY (host_name) REFERENCES tao.execution_node (host_name) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.execution_node_service ADD CONSTRAINT FK_execution_node_service_service
    FOREIGN KEY (service_id) REFERENCES tao.service (id) ON DELETE No Action ON UPDATE No Action;

ALTER TABLE tao.execution_node_service ADD CONSTRAINT FK_execution_node_service_service_status
	FOREIGN KEY (service_status_id) REFERENCES tao.service_status (id) ON DELETE No Action ON UPDATE No Action;
-------------------------------------------------------------------------------
