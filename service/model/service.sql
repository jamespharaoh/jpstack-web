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

-- service_create_missing

CREATE OR REPLACE FUNCTION service_create_missing (
	text,
	text,
	bool
) RETURNS void AS $$
DECLARE

	the_object_type_code_like ALIAS FOR $1;
	the_service_type_code_like ALIAS FOR $2;
	the_actually_create ALIAS FOR $3;

	the_object_type record;
	the_service_type record;

	has_shown_object_type bool;
	has_shown_service_type bool;

	the_record record;

	the_count int;

BEGIN

	RAISE NOTICE 'Creating missing services for all objects';

	the_count := 0;

	FOR the_object_type IN
		SELECT * FROM object_type
		WHERE the_object_type_code_like IS NULL
		OR code LIKE the_object_type_code_like
		ORDER BY code
	LOOP

		has_shown_object_type := false;

		FOR the_service_type IN
			SELECT *
			FROM service_type
			WHERE parent_object_type_id = the_object_type.id
			AND (
				the_service_type_code_like IS NULL
				OR code LIKE the_service_type_code_like
			)
			ORDER BY code
		LOOP

			has_shown_service_type := false;

			FOR the_record IN EXECUTE
				'SELECT parent.* ' ||
				'FROM ' || quote_ident (the_object_type.code) || ' parent ' ||
				'LEFT JOIN service ' ||
				'ON service.type_id = ' || the_service_type.id || ' ' ||
				'AND service.code = ' || quote_literal (the_service_type.code) || ' ' ||
				'AND service.parent_object_type_id = ' || the_service_type.parent_object_type_id || ' ' ||
				'AND service.parent_object_id = parent.id ' ||
				'WHERE service.id IS NULL'
			LOOP

				IF NOT has_shown_object_type THEN

					RAISE NOTICE
						'- Object type %',
						the_object_type.code;

					has_shown_object_type := true;

				END IF;

				IF NOT has_shown_service_type THEN

					RAISE NOTICE
						'  - service type %',
						the_service_type.code;
					has_shown_service_type := true;

				END IF;

				RAISE NOTICE
					'    - Object % (%)', the_record.code,
					the_record.id;

				IF the_actually_create THEN

					INSERT INTO service (
						type_id,
						parent_object_type_id,
						parent_object_id,
						code)
					SELECT
						the_service_type.id,
						the_object_type.id,
						the_record.id,
						the_service_type.code;

				END IF;

				the_count := the_count + 1;

			END LOOP;

		END LOOP;

	END LOOP;

	RAISE NOTICE
		'Created % missing services',
		the_count;

END;
$$ LANGUAGE plpgsql;
