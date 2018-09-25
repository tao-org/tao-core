-------------------------------------------------------------------------------
-- table: tao.group
DROP TABLE IF EXISTS tao."group" CASCADE;
CREATE TABLE tao."group"
(
	id integer NOT NULL,
	name varchar(50) NOT NULL
);
ALTER TABLE tao."group" ADD CONSTRAINT PK_group PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.user_status
DROP TABLE IF EXISTS tao.user_status CASCADE;
CREATE TABLE tao.user_status
(
	id integer NOT NULL,
	status varchar(250) NOT NULL
);
ALTER TABLE tao.user_status ADD CONSTRAINT PK_user_status PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tao.user
DROP TABLE IF EXISTS tao."user" CASCADE;
CREATE TABLE tao."user"
(
	id integer NOT NULL,
	username varchar(50) NOT NULL,
	-- password nullable for external users
	password text NULL,
	email varchar(100) NOT NULL,
	alternative_email varchar(100) NULL,
	last_name varchar(50) NOT NULL,
	first_name varchar(50) NOT NULL,
	phone varchar(50) NULL,
	last_login_date timestamp NULL,
	quota real NOT NULL,
	organization varchar(255) NOT NULL,
	status_id integer NOT NULL,
	external boolean NULL DEFAULT false,
	password_reset_key varchar(255) NULL,
	created timestamp NULL DEFAULT now(),
	modified timestamp NULL
);
ALTER TABLE tao."user" ADD CONSTRAINT PK_user PRIMARY KEY (id);
ALTER TABLE tao."user" ADD CONSTRAINT UQ_user UNIQUE (username);
ALTER TABLE tao."user" ADD CONSTRAINT FK_user_status
	FOREIGN KEY (status_id) REFERENCES tao.user_status (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.user_group
DROP TABLE IF EXISTS tao.user_group CASCADE;
CREATE TABLE tao.user_group
(
	user_id integer NOT NULL,
	group_id integer NOT NULL
);
ALTER TABLE tao.user_group ADD CONSTRAINT PK_user_group PRIMARY KEY (user_id, group_id);
ALTER TABLE tao.user_group ADD CONSTRAINT FK_user_group_user
	FOREIGN KEY (user_id) REFERENCES tao."user" (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE tao.user_group ADD CONSTRAINT FK_user_group_group
	FOREIGN KEY (group_id) REFERENCES tao."group" (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: tao.user_prefs
DROP TABLE IF EXISTS tao.user_prefs CASCADE;
CREATE TABLE tao.user_prefs
(
	user_id integer NOT NULL,
	pref_key varchar(50) NOT NULL,
	pref_value varchar(250) NOT NULL
);
ALTER TABLE tao.user_prefs ADD CONSTRAINT PK_user_prefs PRIMARY KEY (user_id, pref_key);
ALTER TABLE tao.user_prefs ADD CONSTRAINT FK_user_prefs_user
	FOREIGN KEY (user_id) REFERENCES tao."user" (id) ON DELETE No Action ON UPDATE No Action;