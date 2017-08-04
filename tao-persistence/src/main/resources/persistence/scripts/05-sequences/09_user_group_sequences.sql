----- table: tao."group" --------------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.group_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.group_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao."group" ALTER COLUMN id SET DEFAULT nextval('tao.group_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.group_id_seq OWNED BY tao."group".id;


----- table: tao."user" ---------------------------------------------------------------------------
DROP SEQUENCE IF EXISTS tao.user_id_seq CASCADE;
-- create sequence
CREATE SEQUENCE tao.user_id_seq 
	INCREMENT BY 1 MINVALUE 1 NO MAXVALUE START WITH 1 NO CYCLE;

-- assign sequence to column
ALTER TABLE tao."user" ALTER COLUMN id SET DEFAULT nextval('tao.user_id_seq');

-- assign column to sequence
ALTER SEQUENCE tao.user_id_seq OWNED BY tao."user".id;

