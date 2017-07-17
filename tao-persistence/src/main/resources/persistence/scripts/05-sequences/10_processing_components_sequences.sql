----- table: tao.parameter_type -------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.parameter_type_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.parameter_type_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.parameter_type ALTER COLUMN id SET DEFAULT nextval('tao.parameter_type_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.parameter_type_id_seq OWNED BY tao.parameter_type.id;


----- table: tao.parameter ------------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.parameter_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.parameter_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.parameter ALTER COLUMN id SET DEFAULT nextval('tao.parameter_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.parameter_id_seq OWNED BY tao.parameter.id;


----- table: tao.operation_source -----------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.operation_source_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.operation_source_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.operation_source ALTER COLUMN id SET DEFAULT nextval('tao.operation_source_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.operation_source_id_seq OWNED BY tao.operation_source.id;


----- table: tao.processing_operation -------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.processing_operation_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.processing_operation_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.processing_operation ALTER COLUMN id SET DEFAULT nextval('tao.processing_operation_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.processing_operation_id_seq OWNED BY tao.processing_operation.id;


----- table: tao.component_visibility -------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.component_visibility_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.component_visibility_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.component_visibility ALTER COLUMN id SET DEFAULT nextval('tao.component_visibility_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.component_visibility_id_seq OWNED BY tao.component_visibility.id;


----- table: tao.processing_component -------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.processing_component_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.processing_component_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.processing_component ALTER COLUMN id SET DEFAULT nextval('tao.processing_component_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.processing_component_id_seq OWNED BY tao.processing_component.id;


