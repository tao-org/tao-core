-------------------------------------------------------------------------------
-- table: execution.execution_status
DROP TABLE IF EXISTS execution.status CASCADE;
CREATE TABLE execution.status
(
	id integer NOT NULL,
	description varchar(50) NOT NULL
);
ALTER TABLE execution.status ADD CONSTRAINT PK_execution_status PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS execution.execution_status_id_seq CASCADE;
CREATE SEQUENCE execution.execution_status_id_seq	INCREMENT BY 1 MINVALUE 0 NO MAXVALUE START WITH 0 NO CYCLE;
ALTER TABLE execution.status ALTER COLUMN id SET DEFAULT nextval('execution.execution_status_id_seq');
ALTER SEQUENCE execution.execution_status_id_seq OWNED BY execution.status.id;

-------------------------------------------------------------------------------
-- table: job
DROP TABLE IF EXISTS execution.job CASCADE;
CREATE TABLE execution.job
(
	id bigint NOT NULL,
	name text NOT NULL,
	job_type smallint NOT NULL DEFAULT 0,
	app_id varchar(50),
	start_time timestamp without time zone NULL,
	end_time timestamp without time zone NULL,
	workflow_id bigint,
	username varchar(50) NOT NULL,
	output_path varchar(512),
	query_id bigint NULL,
	execution_status_id integer NOT NULL,
	is_external boolean NULL DEFAULT false,
	callback json NULL,
	task_dependencies json NULL,
	batch_id varchar(50)
);
ALTER TABLE execution.job ADD CONSTRAINT PK_job PRIMARY KEY (id);
ALTER TABLE execution.job ADD CONSTRAINT FK_job_workflow
	FOREIGN KEY (workflow_id) REFERENCES workflow.graph (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE execution.job ADD CONSTRAINT FK_job_user
	FOREIGN KEY (username) REFERENCES usr."user" (username) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE execution.job ADD CONSTRAINT FK_job_query
	FOREIGN KEY (query_id) REFERENCES workflow.query (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE execution.job ADD CONSTRAINT FK_job_execution_status
	FOREIGN KEY (execution_status_id) REFERENCES execution.status (id) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS execution.job_id_seq CASCADE;
CREATE SEQUENCE execution.job_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE execution.job ALTER COLUMN id SET DEFAULT nextval('execution.job_id_seq');
ALTER SEQUENCE execution.job_id_seq OWNED BY execution.job.id;

-------------------------------------------------------------------------------
-- table: execution.task
DROP TABLE IF EXISTS execution.task CASCADE;
CREATE TABLE execution.task
(
	id bigint NOT NULL,
	-- FK to either execution.processing_component, either execution.data_source_component,
	-- Also nullable, because execution group doesn't have a component
	component_id varchar(512) NULL,
	resource_id varchar(512) NULL,
	graph_node_id bigint,
	start_time timestamp without time zone NULL,
	end_time timestamp without time zone NULL,
	last_updated timestamp without time zone NULL,
	internal_state varchar(512) NULL,
	-- for some tasks the parent is the task group, not the job, therefore job_id nullable
	job_id bigint NULL,
	task_group_id bigint NULL,
	execution_node_host_name varchar(250) NULL,
	command varchar NULL,
	execution_status_id integer NOT NULL,
	execution_level integer NOT NULL,
	execution_log varchar NULL,
	instance_id integer NOT NULL,
	used_CPU integer NULL,
    used_RAM integer NULL,
    used_HDD integer NULL,
    temp_target text NULL,
    actual_target text NULL,
    cardinality integer NULL DEFAULT(1),
    -- special column used by JPA to distinguish which type of object is stored in one row (since this table holds 2 types of entities)
    discriminator integer NOT NULL
);
ALTER TABLE execution.task ADD CONSTRAINT PK_task PRIMARY KEY (id);
-- This prevents deleting a node if previously executed
--ALTER TABLE execution.task ADD CONSTRAINT FK_task_graph_node
--	FOREIGN KEY (graph_node_id) REFERENCES execution.graph_node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE execution.task ADD CONSTRAINT FK_task_job
	FOREIGN KEY (job_id) REFERENCES execution.job (id) ON DELETE CASCADE ON UPDATE No Action;
ALTER TABLE execution.task ADD CONSTRAINT FK_task_group
	FOREIGN KEY (task_group_id) REFERENCES execution.task (id) ON DELETE CASCADE ON UPDATE No Action;
--ALTER TABLE execution.task ADD CONSTRAINT FK_task_execution_node
--	FOREIGN KEY (execution_node_host_name) REFERENCES topology.node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE execution.task ADD CONSTRAINT FK_task_execution_status
	FOREIGN KEY (execution_status_id) REFERENCES execution.status (id) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS execution.task_id_seq CASCADE;
CREATE SEQUENCE execution.task_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE execution.task ALTER COLUMN id SET DEFAULT nextval('execution.task_id_seq');
ALTER SEQUENCE execution.task_id_seq OWNED BY execution.task.id;

-------------------------------------------------------------------------------
-- table: execution.execution_group_tasks
DROP TABLE IF EXISTS execution.task_group CASCADE;
CREATE TABLE execution.task_group
(
    group_id bigint NOT NULL,
	task_id bigint NOT NULL
);
ALTER TABLE execution.task_group ADD CONSTRAINT PK_execution_group_tasks
	PRIMARY KEY (group_id, task_id);
ALTER TABLE execution.task_group ADD CONSTRAINT FK_execution_group_tasks_execution_group
	FOREIGN KEY (group_id) REFERENCES execution.task (id) ON DELETE CASCADE ON UPDATE No Action;
ALTER TABLE execution.task_group ADD CONSTRAINT FK_execution_group_tasks_execution_task
	FOREIGN KEY (task_id) REFERENCES execution.task (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: execution.task_inputs
DROP TABLE IF EXISTS execution.task_input CASCADE;
CREATE TABLE execution.task_input
(
	task_id bigint NOT NULL,
	key varchar(512) NOT NULL,
	value varchar NULL
);
ALTER TABLE execution.task_input ADD CONSTRAINT PK_task_inputs PRIMARY KEY (task_id, key);
ALTER TABLE execution.task_input ADD CONSTRAINT FK_task_inputs_task
	FOREIGN KEY (task_id) REFERENCES execution.task (id) ON DELETE CASCADE ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: execution.task_output
DROP TABLE IF EXISTS execution.task_output CASCADE;
CREATE TABLE execution.task_output
(
	task_id bigint NOT NULL,
	key varchar(512) NOT NULL,
    value varchar NULL
);
ALTER TABLE execution.task_output ADD CONSTRAINT PK_task_output PRIMARY KEY (task_id, key);
ALTER TABLE execution.task_output ADD CONSTRAINT FK_task_output_task
	FOREIGN KEY (task_id) REFERENCES execution.task (id) ON DELETE CASCADE ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: execution.user_data_query
DROP TABLE IF EXISTS execution.user_data_query CASCADE;
CREATE TABLE execution.user_data_query
(
    id bigint NOT NULL,
    job_id bigint NOT NULL,
	query_id integer NOT NULL
);
ALTER TABLE execution.user_data_query ADD CONSTRAINT PK_user_data_query
	PRIMARY KEY (job_id, query_id);
ALTER TABLE execution.user_data_query ADD CONSTRAINT FK_user_data_query_query
	FOREIGN KEY (query_id) REFERENCES workflow.query (id) ON DELETE CASCADE ON UPDATE No Action;
ALTER TABLE execution.user_data_query ADD CONSTRAINT FK_user_data_query_job
	FOREIGN KEY (job_id) REFERENCES execution.job (id) ON DELETE CASCADE ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: execution.user_data_source_connection
DROP TABLE IF EXISTS execution.user_data_source_connection CASCADE;
CREATE TABLE execution.user_data_source_connection
(
    id bigserial NOT NULL,
	user_id varchar(50) NOT NULL,
    data_source_id varchar(512) NOT NULL,
	username varchar(50) NULL,
	password text NULL,
	params varchar(512) NULL,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE execution.user_data_source_connection ADD CONSTRAINT PK_user_data_source_connection
	PRIMARY KEY (id);
ALTER TABLE execution.user_data_source_connection ADD CONSTRAINT U_user_data_source_connection UNIQUE (user_id, data_source_id);
ALTER TABLE execution.user_data_source_connection ADD CONSTRAINT FK_user_data_source_connection_user
	FOREIGN KEY (user_id) REFERENCES usr."user" (username) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS execution.user_data_source_connection_id_seq CASCADE;
CREATE SEQUENCE execution.user_data_source_connection_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE execution.user_data_source_connection ALTER COLUMN id SET DEFAULT nextval('execution.user_data_source_connection_id_seq');
ALTER SEQUENCE execution.user_data_source_connection_id_seq OWNED BY execution.user_data_source_connection.id;

-- Table: execution.component_time
DROP TABLE IF EXISTS execution.component_time;
CREATE TABLE execution.component_time
(
    component_id varchar(512) NOT NULL,
    average_duration_seconds integer NOT NULL,
    CONSTRAINT component_time_pkey PRIMARY KEY (component_id),
    CONSTRAINT fk_component_time_processing_component FOREIGN KEY (component_id)
        REFERENCES component.processing_component (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS fki_fk_component_time_processing_component
    ON execution.component_time USING btree
    (component_id COLLATE pg_catalog."default" ASC NULLS LAST);