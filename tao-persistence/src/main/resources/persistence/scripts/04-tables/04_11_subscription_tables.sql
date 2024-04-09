-------------------------------------------------------------------------------
-- table: subscription.workflow
DROP TABLE IF EXISTS subscription.workflow;
CREATE TABLE IF NOT EXISTS subscription.workflow
(
    id bigint NOT NULL,
    user_id varchar NOT NULL,
    workflow_id bigint NOT NULL,
    created timestamp without time zone NULL DEFAULT now()
);
ALTER TABLE subscription.workflow ADD CONSTRAINT PK_subscription_workflow PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS subscription.workflow_id_seq CASCADE;
CREATE SEQUENCE subscription.workflow_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE subscription.workflow ALTER COLUMN id SET DEFAULT nextval('subscription.workflow_id_seq');
ALTER SEQUENCE subscription.workflow_id_seq OWNED BY subscription.workflow.id;

-------------------------------------------------------------------------------
-- table: subscription.dataset
DROP TABLE IF EXISTS subscription.dataset;
CREATE TABLE IF NOT EXISTS subscription.dataset
(
    id bigint NOT NULL,
    user_id varchar NOT NULL,
    repository_id varchar NOT NULL,
    name varchar NOT NULL,
    data_root_path varchar NOT NULL,
    checksum varchar,
    subscribers_count integer DEFAULT 0,
    created timestamp without time zone NULL DEFAULT now()
);
ALTER TABLE subscription.dataset ADD CONSTRAINT PK_subscription_dataset PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS subscription.dataset_id_seq CASCADE;
CREATE SEQUENCE subscription.dataset_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE subscription.dataset ALTER COLUMN id SET DEFAULT nextval('subscription.dataset_id_seq');
ALTER SEQUENCE subscription.dataset_id_seq OWNED BY subscription.dataset.id;

-------------------------------------------------------------------------------
-- table: subscription.subscription_type
DROP TABLE IF EXISTS subscription.subscription_type;
CREATE TABLE IF NOT EXISTS subscription.subscription_type
(
    id smallint NOT NULL,
    name varchar NOT NULL
);
ALTER TABLE subscription.subscription_type ADD CONSTRAINT PK_subscription_type PRIMARY KEY (id);

-------------------------------------------------------------------------------
-- table: subscription.resource_subscription
DROP TABLE IF EXISTS subscription.resource_subscription;
CREATE TABLE IF NOT EXISTS subscription.resource_subscription
(
    id bigint NOT NULL,
    user_id varchar NOT NULL,
    type_id smallint NOT NULL,
    flavor_id varchar NOT NULL,
    flavor_quantity smallint NOT NULL,
    flavor_hdd_quantity_GB smallint,
    flavor_ssd_quantity_GB smallint,
    object_storage_GB smallint,
    paid boolean NULL DEFAULT false,
    created timestamp without time zone NULL DEFAULT now(),
    ended timestamp without time zone NULL
);
ALTER TABLE subscription.resource_subscription ADD CONSTRAINT PK_resource_subscription PRIMARY KEY (id);
DROP SEQUENCE IF EXISTS subscription.resource_subscription_id_seq CASCADE;
CREATE SEQUENCE subscription.resource_subscription_id_seq INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;
ALTER TABLE subscription.resource_subscription ALTER COLUMN id SET DEFAULT nextval('subscription.resource_subscription_id_seq');
ALTER SEQUENCE subscription.resource_subscription_id_seq OWNED BY subscription.resource_subscription.id;
ALTER TABLE subscription.resource_subscription ADD CONSTRAINT FK_resource_subscription_type
    FOREIGN KEY (type_id) REFERENCES subscription.subscription_type (id) ON DELETE No Action ON UPDATE No Action;