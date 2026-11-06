-------------------------------------------------------------------------------
-- table: config.category
DROP TABLE IF EXISTS config.category;
CREATE TABLE IF NOT EXISTS config.category
(
    id smallint NOT NULL,
    name varchar NOT NULL,
    display_order integer NOT NULL
);
ALTER TABLE config.category ADD CONSTRAINT PK_config_category PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: config.config
DROP TABLE IF EXISTS config.config;
CREATE TABLE IF NOT EXISTS config.config
(
    id varchar(512) NOT NULL,
    value varchar,
    friendly_name varchar NOT NULL,
    type varchar(10) NOT NULL,
    category_id smallint NOT NULL,
    label varchar,
    "values" json,
    depends_on_item_id varchar(512),
    depends_on_item_value varchar,
    per_user boolean NOT NULL DEFAULT(false),
    last_updated timestamp with time zone NOT NULL DEFAULT now()
);
ALTER TABLE config.config ADD CONSTRAINT PK_config PRIMARY KEY (id);
ALTER TABLE config.config ADD CONSTRAINT FK_config_category
    FOREIGN KEY (category_id) REFERENCES config.category (id) ON DELETE No Action ON UPDATE No Action;
ALTER TABLE config.config ADD CONSTRAINT FK_config_config
    FOREIGN KEY (depends_on_item_id) REFERENCES config.config (id) ON DELETE No Action ON UPDATE No Action;

-------------------------------------------------------------------------------
-- table: config.user_config
DROP TABLE IF EXISTS config.user_config;
CREATE TABLE IF NOT EXISTS config.user_config
(
    user_id varchar(50) NOT NULL,
    id varchar(512) NOT NULL,
    value varchar,
    friendly_name varchar NOT NULL,
    type varchar(10) NOT NULL,
    category_id smallint NOT NULL,
    label varchar,
    "values" json,
    depends_on_item_id varchar(512),
    depends_on_item_value varchar,
    last_updated timestamp with time zone NOT NULL DEFAULT now()
);
ALTER TABLE config.user_config ADD CONSTRAINT PK_user_config PRIMARY KEY (user_id, id);
ALTER TABLE config.user_config ADD CONSTRAINT FK_user_config_category
    FOREIGN KEY (category_id) REFERENCES config.category (id) ON DELETE No Action ON UPDATE No Action;