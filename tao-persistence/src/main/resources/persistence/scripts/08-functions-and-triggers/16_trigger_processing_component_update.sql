-- trigger that updates the "modified" column value in "tao.processing_component" table when a node is updated

-- the trigger function
CREATE OR REPLACE FUNCTION tao.processing_component_update()
  RETURNS trigger AS
$BODY$
BEGIN
 IF NEW IS DISTINCT FROM OLD THEN
   UPDATE tao.processing_component SET modified = now() WHERE tao.processing_component.id = NEW.id;
 END IF;
 
 RETURN NEW;
END;
$BODY$

LANGUAGE plpgsql VOLATILE
COST 100;

-- the trigger itself
CREATE TRIGGER processing_component_update_trigger AFTER UPDATE
ON tao.processing_component
FOR EACH ROW EXECUTE PROCEDURE tao.processing_component_update();