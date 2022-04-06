-------------------------------------------------------------------------------
-- table: workspace.repository_type
DROP TABLE IF EXISTS workspace.repository_type CASCADE;
CREATE TABLE workspace.repository_type
(
    id integer NOT NULL,
	name varchar(250) NOT NULL,
	params json NULL
);
ALTER TABLE workspace.repository_type ADD CONSTRAINT PK_repository_type PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: workspace.repository
DROP TABLE IF EXISTS workspace.repository CASCADE;
CREATE TABLE workspace.repository
(
    id varchar(40) DEFAULT uuid_generate_v4(),
	name varchar(250) NOT NULL,
	description text NOT NULL,
	url_prefix varchar(20) NOT NULL,
	username varchar(50),
	repository_type_id int NOT NULL,
	read_only boolean NOT NULL,
	system boolean DEFAULT false,
	params json NULL,
	created timestamp DEFAULT now()
);
ALTER TABLE workspace.repository ADD CONSTRAINT PK_repository PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS workspace.repository_id_seq CASCADE;
ALTER TABLE workspace.repository ADD CONSTRAINT FK_workspace_repository_type
    FOREIGN KEY (repository_type_id) REFERENCES workspace.repository_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: workspace.site
DROP TABLE IF EXISTS workspace.site CASCADE;
CREATE TABLE workspace.site
(
    id varchar(40) DEFAULT uuid_generate_v4(),
	name varchar(250) NOT NULL,
	description text NOT NULL,
	footprint geography(POLYGON, 4326) NOT NULL,
	start_date timestamp NOT NULL,
	end_date timestamp NOT NULL,
	username varchar(50)
);
ALTER TABLE workspace.site ADD CONSTRAINT PK_site PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS workspace.site_id_seq CASCADE;
