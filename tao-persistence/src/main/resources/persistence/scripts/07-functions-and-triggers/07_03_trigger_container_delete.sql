-- trigger that updates the "container_id" column value to NULL in "tao.processing_component" table when a container is deleted

-- the trigger function
CREATE OR REPLACE FUNCTION component.container_delete()
  RETURNS trigger AS
$BODY$
BEGIN
   UPDATE component.processing_component SET container_id = NULL WHERE component.processing_component.container_id = OLD.id;
 RETURN OLD;
END;
$BODY$

LANGUAGE plpgsql VOLATILE
COST 100;


-- the trigger itself
CREATE TRIGGER container_delete_trigger BEFORE DELETE
ON component.container
FOR EACH ROW EXECUTE PROCEDURE component.container_delete();
