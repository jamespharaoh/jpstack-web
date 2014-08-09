
-- FUCTION media_type_insert

CREATE OR REPLACE FUNCTION media_type_insert (
	text,
	text,
	text)
RETURNS int AS $$

DECLARE

	new_mime_type ALIAS FOR $1;
	new_description ALIAS FOR $2;
	new_extension ALIAS FOR $3;

	new_id int;

BEGIN

	SELECT INTO new_id id
	FROM media_type
	WHERE mime_type = new_mime_type;

	IF new_id IS NOT NULL THEN

		UPDATE media_type
		SET description = new_description,
			extension = new_extension
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id :=
		nextval ('media_type_id_seq');

	INSERT INTO media_type (
		id,
		mime_type,
		description,
		extension)
	VALUES (
		new_id,
		new_mime_type,
		new_description,
		new_extension);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION media_type_insert (
	text,text)
RETURNS int
AS $$

	SELECT media_type_insert (
		$1,
		$2,
		NULL);

$$ LANGUAGE SQL;
