-- trigger that updates the "modified" column value in "tao.processing_component" table when a component is updated

-- the trigger function
CREATE OR REPLACE FUNCTION component.processing_component_update()
  RETURNS trigger AS
$BODY$
BEGIN
 IF NEW IS DISTINCT FROM OLD THEN
   UPDATE component.processing_component SET modified = now() WHERE component.processing_component.id = NEW.id;
 END IF;
 
 RETURN NEW;
END;
$BODY$

LANGUAGE plpgsql VOLATILE
COST 100;

-- the trigger itself
CREATE TRIGGER processing_component_update_trigger AFTER UPDATE
ON component.processing_component
FOR EACH ROW EXECUTE PROCEDURE component.processing_component_update();
