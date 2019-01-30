-- trigger that updates the "modified" column value in "tao.execution_node" table when a node is updated

-- the trigger function
CREATE OR REPLACE FUNCTION topology.execution_node_update()
  RETURNS trigger AS
$BODY$
BEGIN
 IF NEW IS DISTINCT FROM OLD THEN
   UPDATE topology.node SET modified = now() WHERE topology.node.id = NEW.id;
 END IF;
 
 RETURN NEW;
END;
$BODY$

LANGUAGE plpgsql VOLATILE
COST 100;

-- the trigger itself
CREATE TRIGGER execution_node_update_trigger AFTER UPDATE
ON topology.node
FOR EACH ROW EXECUTE PROCEDURE topology.execution_node_update();