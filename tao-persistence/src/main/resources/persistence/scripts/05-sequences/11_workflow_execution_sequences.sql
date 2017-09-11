----- table: tao.workflow_graph_visibility -------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.workflow_graph_visibility_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.workflow_graph_visibility_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.workflow_graph_visibility ALTER COLUMN id SET DEFAULT nextval('tao.workflow_graph_visibility_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.workflow_graph_visibility_id_seq OWNED BY tao.workflow_graph_visibility.id;


-- table: tao.workflow_graph ----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.workflow_graph_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.workflow_graph_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.workflow_graph ALTER COLUMN id SET DEFAULT nextval('tao.workflow_graph_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.workflow_graph_id_seq OWNED BY tao.workflow_graph.id;


----- table: tao.graph_node -----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.graph_node_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.graph_node_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.graph_node ALTER COLUMN id SET DEFAULT nextval('tao.graph_node_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.graph_node_id_seq OWNED BY tao.graph_node.id;


----- table: tao.job_status -----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.job_status_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.job_status_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.job_status ALTER COLUMN id SET DEFAULT nextval('tao.job_status_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.job_status_id_seq OWNED BY tao.job_status.id;


----- table: tao.job ------------------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.job_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.job_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.job ALTER COLUMN id SET DEFAULT nextval('tao.job_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.job_id_seq OWNED BY tao.job.id;


----- table: tao.task_status ----------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.task_status_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.task_status_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.task_status ALTER COLUMN id SET DEFAULT nextval('tao.task_status_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.task_status_id_seq OWNED BY tao.task_status.id;


----- table: tao.task -----------------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.task_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.task_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.task ALTER COLUMN id SET DEFAULT nextval('tao.task_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.task_id_seq OWNED BY tao.task.id;


