---------------------------------------- TABLE service_type

CREATE OR REPLACE FUNCTION service_type_id (text)
RETURNS integer AS $$

	SELECT id
	FROM service_type
	WHERE code = $1;

$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION service_type_insert (text, text, text)
RETURNS integer AS $$

DECLARE
	the_parent_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;
	the_parent_object_type_id int;
	new_id int;
BEGIN

	-- lookup parent object type

	SELECT INTO the_parent_object_type_id id
	FROM object_type
	WHERE code = the_parent_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION
			'Parent object type % not found',
			the_parent_object_type_code;

	END IF;

	-- update existing if found

	SELECT INTO new_id id
	FROM service_type
	WHERE parent_object_type_id = the_parent_object_type_id
		AND code = new_code;

	IF FOUND THEN

		UPDATE service_type
		SET code = new_code,
			description = new_description
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id := nextval ('service_type_id_seq');

	INSERT INTO service_type (id, code, description, parent_object_type_id)
	VALUES (new_id, new_code, new_description, the_parent_object_type_id);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';
