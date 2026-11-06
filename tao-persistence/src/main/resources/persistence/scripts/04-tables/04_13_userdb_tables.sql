-- Table: userdb.templates
DROP TABLE IF EXISTS userdb.templates;
CREATE TABLE IF NOT EXISTS userdb.templates
(
    name character varying(100) NOT NULL,
    description character varying(250) NOT NULL,
    sql character varying NOT NULL
);
ALTER TABLE userdb.templates ADD CONSTRAINT PK_templates PRIMARY KEY (name);

-- Table: userdb.saved_queries
DROP TABLE IF EXISTS userdb.saved_queries;
CREATE TABLE IF NOT EXISTS userdb.saved_queries
(
    name character varying(100) NOT NULL,
    user_id character varying(50) NOT NULL,
    description character varying(250),
    sql character varying NOT NULL,
    created timestamp without time zone DEFAULT now(),
    last_updated timestamp without time zone DEFAULT now()
);
ALTER TABLE userdb.saved_queries ADD CONSTRAINT PK_saved_queries PRIMARY KEY (name, user_id);
ALTER TABLE userdb.saved_queries ADD CONSTRAINT fk_saved_queries_user FOREIGN KEY (user_id)
    REFERENCES usr."user" (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;