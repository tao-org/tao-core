-------------------------------------------------------------------------------
-- table: group
DROP TABLE IF EXISTS tao."group" CASCADE;

CREATE TABLE tao."group"
(
	id integer NOT NULL,
	name varchar(50) NOT NULL
);

ALTER TABLE tao."group" ADD CONSTRAINT PK_group PRIMARY KEY (id);


-------------------------------------------------------------------------------
-- table: user
DROP TABLE IF EXISTS tao."user" CASCADE;

CREATE TABLE tao."user"
(
	id integer NOT NULL,
	username varchar(50) NOT NULL,
	password text NOT NULL,
	email varchar(100) NOT NULL,
	alternative_email varchar(100) NULL,
	last_name varchar(50) NOT NULL,
	first_name varchar(50) NOT NULL,
	phone varchar(50) NULL,
	last_login_date timestamp NULL,
	quota real NOT NULL,
	group_id integer NULL,
	created timestamp NOT NULL,
	modified timestamp NULL,
	active boolean NOT NULL
);

ALTER TABLE tao."user" ADD CONSTRAINT PK_user PRIMARY KEY (id);

ALTER TABLE tao."user" ADD CONSTRAINT FK_user_group
	FOREIGN KEY (group_id) REFERENCES tao."group" (id) ON DELETE No Action ON UPDATE No Action;


-------------------------------------------------------------------------------
-- table: user_prefs
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

