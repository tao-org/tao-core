-- trigger that updates the "container_id" column value to NULL in "tao.processing_component" table when a container is deleted

-- the trigger function
CREATE OR REPLACE FUNCTION tao.container_delete()
  RETURNS trigger AS
$BODY$
BEGIN
   UPDATE tao.processing_component SET container_id = NULL WHERE tao.processing_component.container_id = OLD.id;
 RETURN OLD;
END;
$BODY$

LANGUAGE plpgsql VOLATILE
COST 100;


-- the trigger itself
CREATE TRIGGER container_delete_trigger BEFORE DELETE
ON tao.container
FOR EACH ROW EXECUTE PROCEDURE tao.container_delete();
