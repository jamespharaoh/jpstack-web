
CREATE OR REPLACE FUNCTION queue_type (text)
RETURNS integer AS $$

	SELECT id
	FROM queue_type
	WHERE code = $1;

$$ LANGUAGE SQL;

---------------------------------------- FUNCTION queue_type_insert

CREATE OR REPLACE FUNCTION queue_type_insert (text, text, text, text, text)
RETURNS integer AS $$

DECLARE

	the_parent_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;
	the_subject_object_type_code ALIAS FOR $4;
	the_ref_object_type_code ALIAS FOR $5;

	the_parent_object_type_id int;
	the_subject_object_type_id int;
	the_ref_object_type_id int;
	new_queue_type_id int;

BEGIN

	-- lookup parent object type

	SELECT INTO the_parent_object_type_id id
	FROM object_type
	WHERE code = the_parent_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION 'Object type not found: %',
			the_parent_object_type_code;

		RETURN NULL;

	END IF;

	-- lookup subject object type

	SELECT INTO the_subject_object_type_id id
	FROM object_type
	WHERE code = the_subject_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION 'Object type not found: %',
			the_subject_object_type_code;

		RETURN NULL;

	END IF;

	-- lookup ref object type

	SELECT INTO the_ref_object_type_id id
	FROM object_type
	WHERE code = the_ref_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION 'Object type not found: %',
			the_ref_object_type_code;

		RETURN NULL;

	END IF;

	-- insert queue type

	new_queue_type_id := nextval ('queue_type_id_seq');

	INSERT INTO queue_type (
		id,
		parent_object_type_id,
		code,
		description,
		subject_object_type_id,
		ref_object_type_id
	) VALUES (
		new_queue_type_id,
		the_parent_object_type_id,
		new_code,
		new_description,
		the_subject_object_type_id,
		the_ref_object_type_id
	);

	RETURN new_queue_type_id;

END;

$$ LANGUAGE 'plpgsql';

CREATE UNIQUE INDEX queue_subject_identity_key
ON queue_subject (queue_id, object_id);

CREATE INDEX queue_subject_active
ON queue_subject (queue_id)
WHERE active_items > 0;

CREATE INDEX queue_item_created_time
ON queue_item (created_time);

CREATE INDEX queue_item_processed_time
ON queue_item (processed_time);

CREATE INDEX queue_item_queue_created_time
ON queue_item (queue_id, created_time);

CREATE INDEX queue_item_queue_processed_time
ON queue_item (queue_id, processed_time);

CREATE INDEX queue_item_claim_user
ON queue_item_claim (user_id)
WHERE status = 'c';

CREATE INDEX queue_item_claim_queue_item
ON queue_item_claim (queue_item_id, id);

---------------------------------------- FUNCTION queue_create_missing

CREATE OR REPLACE FUNCTION queue_create_missing (text, text, bool)
RETURNS void AS $$
DECLARE

	the_object_type_code_like ALIAS FOR $1;
	the_queue_type_code_like ALIAS FOR $2;
	the_actually_create ALIAS FOR $3;

	the_object_type record;
	the_queue_type record;

	has_shown_object_type bool;
	has_shown_queue_type bool;

	the_record record;
	the_priv_id int;
	the_count int;

BEGIN

	RAISE NOTICE 'Creating missing queues for all objects';
	the_count := 0;

	FOR the_object_type IN

		SELECT * FROM object_type
		WHERE the_object_type_code_like IS NULL
			OR code LIKE the_object_type_code_like
		ORDER BY code

	LOOP

		has_shown_object_type := false;

		FOR the_queue_type IN

			SELECT * FROM queue_type
			WHERE
				parent_object_type_id = the_object_type.id
				AND (
					the_queue_type_code_like IS NULL
					OR code LIKE the_queue_type_code_like
				)
			ORDER BY code

		LOOP

			has_shown_queue_type := false;

			FOR the_record IN EXECUTE

				'SELECT parent.* ' ||
				'FROM ' || quote_ident (the_object_type.code) || ' parent ' ||
				'LEFT JOIN queue ' ||
					'ON queue.queue_type_id = ' || the_queue_type.id || ' ' ||
						'AND queue.code = ' || quote_literal (the_queue_type.code) || ' ' ||
						'AND queue.parent_object_type_id = ' || the_queue_type.parent_object_type_id || ' ' ||
						'AND queue.parent_object_id = parent.id ' ||
				'WHERE queue.id IS NULL'

			LOOP

				IF NOT has_shown_object_type THEN

					RAISE NOTICE
						'- Object type %',
						the_object_type.code;

					has_shown_object_type := true;

				END IF;

				IF NOT has_shown_queue_type THEN

					RAISE NOTICE
						'  - queue type %',
						the_queue_type.code;
						has_shown_queue_type := true;

				END IF;

				RAISE NOTICE
					'    - Object % (%)',
					the_record.code,
					the_record.id;

				IF the_actually_create THEN

					INSERT INTO queue (
						queue_type_id,
						parent_object_type_id,
						parent_object_id,
						code)
					VALUES (
						the_queue_type.id,
						the_object_type.id,
						the_record.id,
						the_queue_type.code);

				END IF;

				the_count := the_count + 1;

			END LOOP;

		END LOOP;

	END LOOP;

	RAISE NOTICE
		'Created % missing queues',
		the_count;

END;
$$ LANGUAGE plpgsql;
