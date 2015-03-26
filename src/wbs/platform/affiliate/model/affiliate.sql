
CREATE OR REPLACE FUNCTION affiliate_type (text)
RETURNS int AS $$

	SELECT id
	FROM affiliate_type
	WHERE code = $1;

$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION affiliate_type_insert (text, text, text)
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
	FROM affiliate_type
	WHERE parent_object_type_id = the_parent_object_type_id
		AND code = new_code;

	IF FOUND THEN

		UPDATE affiliate_type
		SET code = new_code,
			description = new_description
		WHERE id = new_id;
		RETURN new_id;

	END IF;

	new_id :=
		nextval ('affiliate_type_id_seq');

	INSERT INTO affiliate_type (
		id,
		code,
		description,
		parent_object_type_id)
	VALUES (
		new_id,
		new_code,
		new_description,
		the_parent_object_type_id);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';

-- affiliate_create_missing

CREATE OR REPLACE FUNCTION affiliate_create_missing (
	text,
	text,
	bool
) RETURNS void AS $$
DECLARE

	the_object_type_code_like ALIAS FOR $1;
	the_affiliate_type_code_like ALIAS FOR $2;
	the_actually_create ALIAS FOR $3;

	the_object_type record;
	the_affiliate_type record;

	has_shown_object_type bool;
	has_shown_affiliate_type bool;

	the_record record;

	the_count int;

BEGIN

	RAISE NOTICE 'Creating missing affiliates for all objects';
	the_count := 0;

	FOR the_object_type IN

		SELECT * FROM object_type
		WHERE the_object_type_code_like IS NULL
			OR code LIKE the_object_type_code_like
		ORDER BY code

	LOOP

		has_shown_object_type := false;

		FOR the_affiliate_type IN

			SELECT * FROM affiliate_type
			WHERE parent_object_type_id = the_object_type.id
				AND (the_affiliate_type_code_like IS NULL
					OR code LIKE the_affiliate_type_code_like)
			ORDER BY code

		LOOP

			has_shown_affiliate_type := false;

			FOR the_record IN EXECUTE

				'SELECT parent.* ' ||
				'FROM ' || quote_ident (the_object_type.code) || ' parent ' ||
				'LEFT JOIN affiliate ' ||
					'ON affiliate.type_id = ' || the_affiliate_type.id || ' ' ||
						'AND affiliate.my_code = ' || quote_literal (the_affiliate_type.code) || ' ' ||
						'AND affiliate.parent_object_type_id = ' || the_affiliate_type.parent_object_type_id || ' ' ||
						'AND affiliate.parent_object_id = parent.id ' ||
				'WHERE affiliate.id IS NULL'

			LOOP

				IF NOT has_shown_object_type THEN

					RAISE NOTICE
						'- Object type %',
						the_object_type.code;
						has_shown_object_type := true;

				END IF;

				IF NOT has_shown_affiliate_type THEN

					RAISE NOTICE
						'  - affiliate type %',
						the_affiliate_type.code;
						has_shown_affiliate_type := true;

				END IF;

				RAISE NOTICE
					'    - Object % (%)',
					the_record.code,
					the_record.id;

				IF the_actually_create THEN

					INSERT INTO affiliate (type_id, parent_object_type_id, parent_object_id, my_code)
					SELECT the_affiliate_type.id, the_object_type.id, the_record.id, the_affiliate_type.code;

				END IF;

				the_count := the_count + 1;

			END LOOP;

		END LOOP;

	END LOOP;

	RAISE NOTICE
		'Created % missing affiliates',
		the_count;

END;
$$ LANGUAGE plpgsql;
