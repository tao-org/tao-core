-- trigger that updates the "modified" column value in "tao.execution_node" table when a node is updated

-- the trigger function
CREATE OR REPLACE FUNCTION tao.execution_node_update()
  RETURNS trigger AS
$BODY$
BEGIN
 IF NEW IS DISTINCT FROM OLD THEN
   UPDATE tao.execution_node SET modified = now() WHERE tao.execution_node.host_name = NEW.host_name;
 END IF;
 
 RETURN NEW;
END;
$BODY$

LANGUAGE plpgsql VOLATILE
COST 100;

-- the trigger itself
CREATE TRIGGER execution_node_update_trigger AFTER UPDATE
ON tao.execution_node
FOR EACH ROW EXECUTE PROCEDURE tao.execution_node_update();