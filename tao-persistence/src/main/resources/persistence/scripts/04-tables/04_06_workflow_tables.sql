-------------------------------------------------------------------------------
-- table: workflow_graph_status
DROP TABLE IF EXISTS workflow.status CASCADE;
CREATE TABLE workflow.status
(
	id integer NOT NULL,
	description varchar(250) NOT NULL
);
ALTER TABLE workflow.status ADD CONSTRAINT PK_workflow_graph_status PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS workflow.graph_status_id_seq CASCADE;
CREATE SEQUENCE workflow.graph_status_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE workflow.status ALTER COLUMN id SET DEFAULT nextval('workflow.graph_status_id_seq');
ALTER SEQUENCE workflow.graph_status_id_seq OWNED BY workflow.status.id;

-------------------------------------------------------------------------------
-- table: workflow.node_behavior
DROP TABLE IF EXISTS workflow.node_behavior CASCADE;
CREATE TABLE workflow.node_behavior
(
	id smallint NOT NULL,
	description varchar(50) NOT NULL
);
ALTER TABLE workflow.node_behavior ADD CONSTRAINT PK_node_behavior PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: workflow.component_type
-- component_type
DROP TABLE IF EXISTS workflow.component_type CASCADE;
CREATE TABLE workflow.component_type
(
	id smallint NOT NULL,
	description varchar(50) NOT NULL
);
ALTER TABLE workflow.component_type ADD CONSTRAINT PK_component_type PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: workflow_graph
DROP TABLE IF EXISTS workflow.graph CASCADE;
CREATE TABLE workflow.graph
(
	id bigint NOT NULL,
	name varchar(250) NOT NULL,
	created timestamp NULL DEFAULT now(),
	username varchar(50) NOT NULL,
	definition_path varchar(512) NULL,
	status_id integer NOT NULL,
	visibility_id integer NOT NULL,
	xCoord real NULL,
    yCoord real NULL,
    zoom real NULL,
	custom_values json NULL,
	tags text NULL,
	active boolean NULL DEFAULT true
);
ALTER TABLE workflow.graph ADD CONSTRAINT PK_workflow PRIMARY KEY (id);
ALTER TABLE workflow.graph ADD CONSTRAINT FK_workflow_graph_status
	FOREIGN KEY (status_id) REFERENCES workflow.status (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.graph ADD CONSTRAINT FK_workflow_graph_visibility
	FOREIGN KEY (visibility_id) REFERENCES common.visibility (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.graph ADD CONSTRAINT FK_workflow_user
	FOREIGN KEY (username) REFERENCES usr."user" (username) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS workflow.workflow_graph_id_seq CASCADE;
CREATE SEQUENCE workflow.workflow_graph_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE workflow.graph ALTER COLUMN id SET DEFAULT nextval('workflow.workflow_graph_id_seq');
ALTER SEQUENCE workflow.workflow_graph_id_seq OWNED BY workflow.graph.id;

-------------------------------------------------------------------------------
-- table: graph_node
DROP TABLE IF EXISTS workflow.graph_node CASCADE;
CREATE TABLE workflow.graph_node
(
	id bigint NOT NULL,
	name varchar(250) NULL,
	created timestamp NULL DEFAULT now(),
	workflow_id bigint NOT NULL,
	node_level integer NOT NULL,
	component_id varchar(512) NOT NULL,
    component_type_id smallint NOT NULL,
	xCoord real NULL,
	yCoord real NULL,
	behavior_id smallint NULL DEFAULT 1,
	preserve_output boolean NULL DEFAULT true,
	custom_values json NULL,
    -- special column used by JPA to distinguish which type of object is stored in one row (since this table holds 2 types of entities)
    discriminator integer NOT NULL
);
ALTER TABLE workflow.graph_node ADD CONSTRAINT PK_graph_node PRIMARY KEY (id);
ALTER TABLE workflow.graph_node ADD CONSTRAINT FK_graph_node_workflow_graph
	FOREIGN KEY (workflow_id) REFERENCES workflow.graph (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.graph_node ADD CONSTRAINT FK_graph_node_component_type
	FOREIGN KEY (component_type_id) REFERENCES workflow.component_type (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.graph_node ADD CONSTRAINT FK_graph_node_node_behavior
	FOREIGN KEY (behavior_id) REFERENCES workflow.node_behavior (id) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS workflow.graph_node_id_seq CASCADE;
CREATE SEQUENCE workflow.graph_node_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE workflow.graph_node ALTER COLUMN id SET DEFAULT nextval('workflow.graph_node_id_seq');
ALTER SEQUENCE workflow.graph_node_id_seq OWNED BY workflow.graph_node.id;	

-------------------------------------------------------------------------------
-- table: graph_node_group_nodes
DROP TABLE IF EXISTS workflow.graph_node_group_nodes CASCADE;
CREATE TABLE workflow.graph_node_group_nodes
(
    graph_node_group_id bigint NOT NULL,
	graph_node_id bigint NOT NULL
);
ALTER TABLE workflow.graph_node_group_nodes ADD CONSTRAINT PK_graph_node_group_nodes
	PRIMARY KEY (graph_node_group_id, graph_node_id);
ALTER TABLE workflow.graph_node_group_nodes ADD CONSTRAINT FK_graph_node_group_nodes_graph_node_01
	FOREIGN KEY (graph_node_group_id) REFERENCES workflow.graph_node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.graph_node_group_nodes ADD CONSTRAINT FK_graph_node_group_nodes_graph_node_02
	FOREIGN KEY (graph_node_id) REFERENCES workflow.graph_node (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: workflow.component_link
DROP TABLE IF EXISTS workflow.component_link CASCADE;
CREATE TABLE workflow.component_link
(
    target_graph_node_id bigint NOT NULL,
    source_descriptor_id varchar(512) NOT NULL,
    source_graph_node_id bigint NOT NULL,
    target_descriptor_id varchar(512) NOT NULL,
    aggregator varchar(512) NULL
);
ALTER TABLE workflow.component_link ADD CONSTRAINT PK_component_link PRIMARY KEY (source_graph_node_id, target_graph_node_id, source_descriptor_id, target_descriptor_id);
ALTER TABLE workflow.component_link ADD CONSTRAINT FK_component_link_graph_node_1
	FOREIGN KEY (source_graph_node_id) REFERENCES workflow.graph_node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.component_link ADD CONSTRAINT FK_component_link_graph_node_2
	FOREIGN KEY (target_graph_node_id) REFERENCES workflow.graph_node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.component_link ADD CONSTRAINT FK_component_link_source_descriptor
	FOREIGN KEY (source_descriptor_id) REFERENCES component.source_descriptor (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.component_link ADD CONSTRAINT FK_component_link_target_descriptor
	FOREIGN KEY (target_descriptor_id) REFERENCES component.target_descriptor (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: query
DROP TABLE IF EXISTS workflow.query CASCADE;
CREATE TABLE workflow.query
(
    id bigserial NOT NULL,
    label text,
	user_id varchar(50) NOT NULL,
	graph_node_id bigint,
	component_id varchar(512),
	sensor_name varchar(512) NOT NULL,
	data_source varchar(512) NOT NULL,
	username varchar NULL,
	password varchar NULL,
	page_size integer NULL,
	page_number integer NULL,
	_limit integer NULL,  -- reserved word
	"values" json NULL,    -- reserved word
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL
);
ALTER TABLE workflow.query ADD CONSTRAINT PK_query
	PRIMARY KEY (id);
ALTER TABLE workflow.query ADD CONSTRAINT U_query UNIQUE (user_id, graph_node_id, sensor_name, data_source);
ALTER TABLE workflow.query ADD CONSTRAINT U_query_label UNIQUE(label, user_id);
ALTER TABLE workflow.query ADD CONSTRAINT FK_query_user
	FOREIGN KEY (user_id) REFERENCES usr."user"(username) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.query ADD CONSTRAINT FK_query_graph_node
	FOREIGN KEY (graph_node_id) REFERENCES workflow.graph_node(id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE workflow.query ADD CONSTRAINT FK_query_data_source_component
    FOREIGN KEY (component_id) REFERENCES component.data_source_component (id) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS workflow.query_id_seq CASCADE;
CREATE SEQUENCE workflow.query_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE workflow.query ALTER COLUMN id SET DEFAULT nextval('workflow.query_id_seq');
ALTER SEQUENCE workflow.query_id_seq OWNED BY workflow.query.id;
	
-------------------------------------------------------------------------------
-- table: workflow.query_parameter
DROP TABLE IF EXISTS workflow.query_parameter CASCADE;
CREATE TABLE workflow.query_parameter
(
	id integer NOT NULL,
	data_type_id integer NOT NULL,
	name varchar(250) NOT NULL
);
ALTER TABLE workflow.query_parameter ADD CONSTRAINT PK_query_parameter
	PRIMARY KEY (id);
ALTER TABLE workflow.query_parameter ADD CONSTRAINT FK_query_parameter_data_type
	FOREIGN KEY (data_type_id) REFERENCES product.data_type (id) ON DELETE No Action ON UPDATE No Action;
DROP SEQUENCE IF EXISTS workflow.query_parameter_id_seq CASCADE;
CREATE SEQUENCE workflow.query_parameter_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE workflow.query_parameter ALTER COLUMN id SET DEFAULT nextval('workflow.query_parameter_id_seq');
ALTER SEQUENCE workflow.query_parameter_id_seq OWNED BY workflow.query_parameter.id;
