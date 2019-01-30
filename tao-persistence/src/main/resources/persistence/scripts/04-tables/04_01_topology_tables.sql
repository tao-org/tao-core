-------------------------------------------------------------------------------
-- table: topology.execution_node
DROP TABLE IF EXISTS topology.node CASCADE;
CREATE TABLE topology.node
(
	id varchar(250) NOT NULL,
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
	tags text NULL,
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL,
	active boolean NULL DEFAULT true
);
ALTER TABLE topology.node ADD CONSTRAINT PK_execution_node PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- topology.service_status
DROP TABLE IF EXISTS topology.service_status CASCADE;
CREATE TABLE topology.service_status
(
	id integer NOT NULL,
	status varchar(50) NOT NULL
);
ALTER TABLE topology.service_status ADD CONSTRAINT PK_service_status PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS topology.service_status_id_seq CASCADE;
CREATE SEQUENCE topology.service_status_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE topology.service_status ALTER COLUMN id SET DEFAULT nextval('topology.service_status_id_seq');
ALTER SEQUENCE topology.service_status_id_seq OWNED BY topology.service_status.id;

-------------------------------------------------------------------------------
-- table: topology.service
DROP TABLE IF EXISTS topology.service CASCADE;
CREATE TABLE topology.service
(
    id integer NOT NULL,
	name varchar(250) NOT NULL,
	version varchar(50) NOT NULL,
	description text NULL
);
ALTER TABLE topology.service ADD CONSTRAINT PK_service PRIMARY KEY (id);
ALTER TABLE topology.service ADD CONSTRAINT K_service UNIQUE (name, version);
DROP SEQUENCE IF EXISTS topology.service_id_seq CASCADE;
CREATE SEQUENCE topology.service_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE topology.service ALTER COLUMN id SET DEFAULT nextval('topology.service_id_seq');
ALTER SEQUENCE topology.service_id_seq OWNED BY topology.service.id;

-------------------------------------------------------------------------------
-- table: topology.execution_node_service
DROP TABLE IF EXISTS topology.node_service CASCADE;
CREATE TABLE topology.node_service
(
	host_name varchar(250) NOT NULL,
	service_id integer NOT NULL,
	service_status_id integer NOT NULL
);
ALTER TABLE topology.node_service ADD CONSTRAINT PK_execution_node_service PRIMARY KEY (host_name, service_id);
ALTER TABLE topology.node_service ADD CONSTRAINT FK_execution_node_service_execution_node
	FOREIGN KEY (host_name) REFERENCES topology.node (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE topology.node_service ADD CONSTRAINT FK_execution_node_service_service
    FOREIGN KEY (service_id) REFERENCES topology.service (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE topology.node_service ADD CONSTRAINT FK_execution_node_service_service_status
	FOREIGN KEY (service_status_id) REFERENCES topology.service_status (id) ON DELETE No Action ON UPDATE No Action;