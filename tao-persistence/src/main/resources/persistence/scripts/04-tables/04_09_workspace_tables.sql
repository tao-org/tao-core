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
	repository_type_id int NOT NULL,
	user_id varchar(50),
	read_only boolean NOT NULL,
	system boolean DEFAULT false,
	is_editable boolean NOT NULL,
	params json NULL,
	display_order smallint,
	persistent_storage boolean DEFAULT false,
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
	user_id varchar(50)
);
ALTER TABLE workspace.site ADD CONSTRAINT PK_site PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS workspace.site_id_seq CASCADE;

-------------------------------------------------------------------------------
-- table: workspace.repository_template
DROP TABLE IF EXISTS workspace.repository_template;
CREATE TABLE IF NOT EXISTS workspace.repository_template
(
    id character varying(40) COLLATE pg_catalog."default" NOT NULL DEFAULT uuid_generate_v4(),
    name character varying(250) COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default" NOT NULL,
    url_prefix character varying(20) COLLATE pg_catalog."default" NOT NULL,
    repository_type_id integer NOT NULL,
    params json,
    created timestamp without time zone DEFAULT now(),
    CONSTRAINT pk_repository_template PRIMARY KEY (id),
    CONSTRAINT fk_repository_template_repository_type FOREIGN KEY (repository_type_id)
        REFERENCES workspace.repository_type (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
GRANT ALL ON TABLE workspace.repository_template TO postgres;
GRANT ALL ON TABLE workspace.repository_template TO tao;