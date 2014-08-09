
CREATE OR REPLACE FUNCTION event_type (text)
RETURNS integer AS $$

	SELECT id
	FROM event_type
	WHERE CODE = $1

$$ LANGUAGE SQL STABLE;

---------------------------------------- FUNCTION event_type_insert

CREATE OR REPLACE FUNCTION event_type_insert (text, text)
RETURNS integer AS $$

DECLARE
	new_code ALIAS FOR $1;
	new_description ALIAS FOR $2;
	the_event_type_id int;
BEGIN

	SELECT INTO the_event_type_id id
	FROM event_type
	WHERE code = new_code;

	IF FOUND THEN

		UPDATE event_type
		SET code = new_code,
			description = new_description
		WHERE id = the_event_type_id;

	ELSE

		the_event_type_id := nextval ('event_type_id_seq');

		INSERT INTO event_type (id, code, description)
		VALUES (the_event_type_id, $1, $2);

	END IF;

	RETURN the_event_type_id;

END;
$$ LANGUAGE 'plpgsql';

CREATE INDEX event_link_target
ON event_link (type_id, ref_id);
