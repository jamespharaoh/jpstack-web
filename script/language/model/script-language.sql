
CREATE OR REPLACE FUNCTION script_language_insert (
	text,
	text)
RETURNS int AS $$

DECLARE
	new_code ALIAS FOR $1;
	new_description ALIAS FOR $2;
	new_id int;
BEGIN

	-- check for and update an existing script_language

	new_id := (
		SELECT id
		FROM script_language
		WHERE code = new_code
	);

	IF new_id IS NOT NULL THEN

		UPDATE script_language
		SET description = new_description
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	-- or create a new one

	new_id :=
		nextval ('script_language_id_seq');

	INSERT INTO script_language (id, code, description)
	VALUES (new_id, new_code, new_description);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';
