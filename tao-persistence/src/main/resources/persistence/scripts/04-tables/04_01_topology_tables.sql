-------------------------------------------------------------------------------
-- topology.node_flavor
DROP TABLE IF EXISTS topology.node_flavor CASCADE;
CREATE TABLE topology.node_flavor
(
	id varchar(250) NOT NULL,
	cpu integer NOT NULL,
	memory integer NOT NULL,
	disk integer NOT NULL,
	swap integer NOT NULL,
	rxtx real NOT NULL
);
ALTER TABLE topology.node_flavor ADD CONSTRAINT PK_node_flavor PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: topology.execution_node
DROP TABLE IF EXISTS topology.node CASCADE;
CREATE TABLE topology.node
(
	id varchar(250) NOT NULL,
	username varchar(50) NOT NULL,
	password text NOT NULL,
	flavor_id varchar(250) NOT NULL,
	description text NULL,
	ssh_key text NULL,
	used_CPU integer NULL,
	used_RAM integer NULL,
	used_HDD integer NULL,
	tags text NULL,
	role varchar(6) NULL DEFAULT 'worker',
	created timestamp NULL DEFAULT now(),
    modified timestamp NULL DEFAULT now(),
    volatile boolean NULL DEFAULT false,
	active boolean NULL DEFAULT true,
	owner_id varchar(50),
	app_id varchar(50),
	server_id varchar(50)
);
ALTER TABLE topology.node ADD CONSTRAINT PK_execution_node PRIMARY KEY (id);
ALTER TABLE topology.node ADD CONSTRAINT FK_execution_node_flavor
    FOREIGN KEY (flavor_id) REFERENCES topology.node_flavor (id) ON DELETE No Action ON UPDATE No Action;

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

-------------------------------------------------------------------------------
-- table: topology.volatile_instance
DROP TABLE IF EXISTS topology.volatile_instance;
CREATE TABLE topology.volatile_instance
(
    id bigint NOT NULL,
    node_id character varying NOT NULL,
    user_id character varying NOT NULL,
    created timestamp without time zone NOT NULL,
    destroyed timestamp without time zone,
    average_cpu_load real,
    average_memory real
);
ALTER TABLE topology.volatile_instance ADD CONSTRAINT PK_volatile_instance PRIMARY KEY (id);
CREATE SEQUENCE topology.volatile_instance_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE topology.volatile_instance ALTER COLUMN id SET DEFAULT nextval('topology.volatile_instance_id_seq');
