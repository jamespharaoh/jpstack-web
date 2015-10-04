---------------------------------------- FUNCTION priv_type

CREATE OR REPLACE FUNCTION priv_type (text) RETURNS integer AS $$
	SELECT id FROM priv_type WHERE code = $1
$$ LANGUAGE SQL STABLE;

---------------------------------------- FUNCTION priv_type_insert

CREATE OR REPLACE FUNCTION priv_type_insert (text, text, text, text, bool)
RETURNS integer AS $$

DECLARE
	the_parent_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;
	new_help ALIAS FOR $4;
	new_template ALIAS FOR $5;
	the_parent_object_type_id int;
	new_id int;
BEGIN

	SELECT INTO the_parent_object_type_id id
	FROM object_type
	WHERE code = the_parent_object_type_code;

	IF NOT FOUND THEN
		RAISE EXCEPTION 'Parent object type % not found',
			the_parent_object_type_code;
	END IF;

	SELECT INTO new_id id
	FROM priv_type
	WHERE parent_object_type_id = the_parent_object_type_id
		AND code = new_code;

	IF FOUND THEN

		UPDATE priv_type
		SET description = new_description,
			help = new_help,
			template = new_template
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id := nextval ('priv_type_id_seq');

	INSERT INTO priv_type (id, code, description, help, template,
		parent_object_type_id)
	VALUES (new_id, new_code, new_description, new_help, new_template,
		the_parent_object_type_id);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';

---------------------------------------- FUNCTION priv_create_missing

CREATE OR REPLACE FUNCTION priv_create_missing (text, text, bool, bool)
RETURNS void AS $$
	DECLARE

		the_object_type_code_like ALIAS FOR $1;
		the_priv_type_code_like ALIAS FOR $2;
		the_actually_create ALIAS FOR $3;
		the_make_noise ALIAS FOR $4;

		the_object_type record;
		the_priv_type record;

		has_shown_object_type bool;
		has_shown_priv_type bool;

		the_record record;

		the_count int;

	BEGIN

		IF the_make_noise THEN

			RAISE NOTICE 'Creating missing privs for all objects';

		END IF;

		the_count := 0;

		FOR the_object_type IN

			SELECT * FROM object_type
			WHERE the_object_type_code_like IS NULL
				OR code LIKE the_object_type_code_like
			ORDER BY code

		LOOP

			has_shown_object_type := false;

			FOR the_priv_type IN

				SELECT * FROM priv_type
				WHERE parent_object_type_id = the_object_type.id
					AND (the_priv_type_code_like IS NULL
						OR code LIKE the_priv_type_code_like)
				ORDER BY code

			LOOP

				has_shown_priv_type := false;

				FOR the_record IN EXECUTE
					'SELECT parent.* ' ||
					'FROM ' || quote_ident (the_object_type.code) ||
						' parent ' ||
					'LEFT JOIN priv ' ||
						'ON priv.priv_type_id = ' || the_priv_type.id || ' ' ||
							'AND priv.code = ' ||
								quote_literal (the_priv_type.code) || ' ' ||
							'AND priv.parent_object_type_id = ' ||
								the_priv_type.parent_object_type_id || ' ' ||
							'AND priv.parent_object_id = parent.id ' ||
					'WHERE priv.id IS NULL'
				LOOP

						RAISE NOTICE
							'		- Object % (%) AAA',
							the_record.code,
							the_record.id;

					IF NOT has_shown_object_type AND the_make_noise THEN

						RAISE NOTICE
							'- Object type %',
							the_object_type.code;

						has_shown_object_type := true;

					END IF;

					IF NOT has_shown_priv_type AND the_make_noise THEN

						RAISE NOTICE
							'	- priv type %',
							the_priv_type.code;

						has_shown_priv_type := true;

					END IF;

					IF the_make_noise THEN

						RAISE NOTICE
							'		- Object % (%)',
							the_record.code,
							the_record.id;

					END IF;

					IF the_actually_create THEN

						INSERT INTO priv (
							priv_type_id,
							parent_object_type_id,
							parent_object_id,
							code)
						SELECT
							the_priv_type.id,
							the_object_type.id,
							the_record.id,
							the_priv_type.code;

					END IF;

					the_count := the_count + 1;

				END LOOP;

			END LOOP;

		END LOOP;

		IF the_make_noise THEN

			RAISE NOTICE
				'Created % missing privs',
				the_count;

		END IF;

	END;
$$ LANGUAGE plpgsql;
