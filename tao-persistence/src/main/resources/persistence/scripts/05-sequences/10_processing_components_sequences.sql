----- table: tao.parameter_type -------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.parameter_type_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.parameter_type_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.parameter_type ALTER COLUMN id SET DEFAULT nextval('tao.parameter_type_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.parameter_type_id_seq OWNED BY tao.parameter_type.id;


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


----- table: tao.template_type --------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.template_type_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.template_type_id_seq
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao.template_type ALTER COLUMN id SET DEFAULT nextval('tao.template_type_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.template_type_id_seq OWNED BY tao.template_type.id;


