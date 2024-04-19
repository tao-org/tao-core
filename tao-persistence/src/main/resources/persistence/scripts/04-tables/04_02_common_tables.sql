-------------------------------------------------------------------------------
-- table: tag_type
DROP TABLE IF EXISTS common.tag_type CASCADE;
CREATE TABLE common.tag_type
(
	id integer NOT NULL,
	description varchar(20) NOT NULL
);
ALTER TABLE common.tag_type ADD CONSTRAINT PK_tag_type
	PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: tag
DROP TABLE IF EXISTS common.tag CASCADE;
CREATE TABLE common.tag
(
	id bigserial NOT NULL,
	tag_type_id integer NOT NULL,
	tag text NOT NULL
);
ALTER TABLE common.tag ADD CONSTRAINT PK_tag
	PRIMARY KEY (id);
ALTER TABLE common.tag ADD CONSTRAINT FK_tag_tag_type
	FOREIGN KEY (tag_type_id) REFERENCES common.tag_type (id) ON DELETE No Action ON UPDATE No Action;

DROP SEQUENCE IF EXISTS common.tag_id_seq CASCADE;
CREATE SEQUENCE common.tag_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE common.tag ALTER COLUMN id SET DEFAULT nextval('common.tag_id_seq');
ALTER SEQUENCE common.tag_id_seq OWNED BY common.tag.id;

-------------------------------------------------------------------------------
-- table: common.notification
DROP TABLE IF EXISTS common.notification CASCADE;
CREATE TABLE common.notification
(
    id bigserial NOT NULL,
    timestamp bigint NOT NULL,
    topic character varying NOT NULL,
	user_id character varying NOT NULL,
	read boolean NULL,
	data character varying NOT NULL,
	discriminator integer NOT NULL
);
ALTER TABLE common.notification ADD CONSTRAINT PK_notification PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS common.notification_id_seq CASCADE;
CREATE SEQUENCE common.notification_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE common.notification ALTER COLUMN id SET DEFAULT nextval('common.notification_id_seq');
ALTER SEQUENCE common.notification_id_seq OWNED BY common.notification.id;
CREATE INDEX IX_notification ON common.notification
    USING btree (topic COLLATE pg_catalog."default", user_id COLLATE pg_catalog."default");

-------------------------------------------------------------------------------
-- table: workflow_graph_visibility
DROP TABLE IF EXISTS common.visibility CASCADE;
CREATE TABLE common.visibility
(
	id integer NOT NULL,
	visibility varchar(250) NOT NULL
);
ALTER TABLE common.visibility ADD CONSTRAINT PK_visibility PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS common.visibility_id_seq CASCADE;
CREATE SEQUENCE common.visibility_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE common.visibility ALTER COLUMN id SET DEFAULT nextval('common.visibility_id_seq');
ALTER SEQUENCE common.visibility_id_seq OWNED BY common.visibility.id;

-------------------------------------------------------------------------------
-- table: audit
DROP TABLE IF EXISTS common.audit CASCADE;
CREATE TABLE common.audit
(
    id bigserial NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    user_id character varying NOT NULL,
    event character varying NOT NULL
);
ALTER TABLE common.audit ADD CONSTRAINT PK_audit PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS common.audit_id_seq CASCADE;
CREATE SEQUENCE common.audit_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE common.audit ALTER COLUMN id SET DEFAULT nextval('common.audit_id_seq');
ALTER SEQUENCE common.audit_id_seq OWNED BY common.audit.id;
CREATE INDEX IX_audit ON common.audit
    USING btree (user_id COLLATE pg_catalog."default", timestamp);