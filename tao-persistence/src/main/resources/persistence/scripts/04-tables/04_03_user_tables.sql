-------------------------------------------------------------------------------
-- table: usr.group
DROP TABLE IF EXISTS usr.grp CASCADE;
CREATE TABLE usr.grp
(
	id integer NOT NULL,
	name varchar(50) NOT NULL,
	input_quota int NOT NULL DEFAULT -1,
	processing_quota int NOT NULL DEFAULT -1,
	cpu_quota int NOT NULL DEFAULT 2,
	memory_quota int NOT NULL DEFAULT 8192
);
ALTER TABLE usr.grp ADD CONSTRAINT PK_group PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS usr.group_id_seq CASCADE;
CREATE SEQUENCE usr.group_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE usr.grp ALTER COLUMN id SET DEFAULT nextval('usr.group_id_seq');
ALTER SEQUENCE usr.group_id_seq OWNED BY usr.grp.id;
-------------------------------------------------------------------------------
-- table: usr.user_status
DROP TABLE IF EXISTS usr.user_status CASCADE;
CREATE TABLE usr.user_status
(
	id integer NOT NULL,
	status varchar(250) NOT NULL
);
ALTER TABLE usr.user_status ADD CONSTRAINT PK_user_status PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS usr.user_status_id_seq CASCADE;
CREATE SEQUENCE usr.user_status_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE usr.user_status ALTER COLUMN id SET DEFAULT nextval('usr.user_status_id_seq');
ALTER SEQUENCE usr.user_status_id_seq OWNED BY usr.user_status.id;

-------------------------------------------------------------------------------
-- table: usr.user_status
DROP TABLE IF EXISTS usr.user_type CASCADE;
CREATE TABLE usr.user_type
(
	id integer NOT NULL,
	type varchar(250) NOT NULL
);
ALTER TABLE usr.user_type ADD CONSTRAINT PK_user_type PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: usr.user
DROP TABLE IF EXISTS usr."user" CASCADE;
CREATE TABLE usr."user"
(
	id varchar NOT NULL DEFAULT(uuid_generate_v4()),
	username varchar(50) NOT NULL,
	-- password nullable for external users
	password text NULL,
	email varchar(100) NOT NULL,
	alternative_email varchar(100) NULL,
	last_name varchar(50) NOT NULL,
	first_name varchar(50) NOT NULL,
	phone varchar(50) NULL,
	last_login_date timestamp NULL,
	input_quota int NOT NULL,
	actual_input_quota int NOT NULL,
	processing_quota int NOT NULL,
    actual_processing_quota int NOT NULL,
    cpu_quota int NOT NULL DEFAULT 2,
    memory_quota int NOT NULL DEFAULT 8192,
	organization varchar(255) NOT NULL,
	status_id integer NOT NULL,
	-- external boolean NULL DEFAULT false,
	user_type_id integer NOT NULL,
	password_reset_key varchar(255) NULL,
	created timestamp NULL DEFAULT now(),
	expiration_date timestamp NULL,
	modified timestamp NULL
);
ALTER TABLE usr."user" ADD CONSTRAINT PK_user PRIMARY KEY (id);
ALTER TABLE usr."user" ADD CONSTRAINT UQ_user UNIQUE (username);
ALTER TABLE usr."user" ADD CONSTRAINT FK_user_status
	FOREIGN KEY (status_id) REFERENCES usr.user_status (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE usr."user" ADD CONSTRAINT FK_user_type
    	FOREIGN KEY (user_type_id) REFERENCES usr.user_type (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: usr.user_group
DROP TABLE IF EXISTS usr.user_group CASCADE;
CREATE TABLE usr.user_group
(
	user_id varchar NOT NULL,
	group_id integer NOT NULL
);
ALTER TABLE usr.user_group ADD CONSTRAINT PK_user_group PRIMARY KEY (user_id, group_id);
ALTER TABLE usr.user_group ADD CONSTRAINT FK_user_group_user
	FOREIGN KEY (user_id) REFERENCES usr."user" (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE usr.user_group ADD CONSTRAINT FK_user_group_group
	FOREIGN KEY (group_id) REFERENCES usr.grp (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: usr.user_prefs
DROP TABLE IF EXISTS usr.user_prefs CASCADE;
CREATE TABLE usr.user_prefs
(
	user_id varchar NOT NULL,
	pref_key varchar(50) NOT NULL,
	pref_value varchar(250) NOT NULL
);
ALTER TABLE usr.user_prefs ADD CONSTRAINT PK_user_prefs PRIMARY KEY (user_id, pref_key);
ALTER TABLE usr.user_prefs ADD CONSTRAINT FK_user_prefs_user
	FOREIGN KEY (user_id) REFERENCES usr."user" (id) ON DELETE No Action ON UPDATE No Action;