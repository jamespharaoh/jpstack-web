
CREATE OR REPLACE FUNCTION psychic_template_type_insert (
	text,
	text,
	text)
RETURNS int
AS $$

DECLARE

	new_template_type_code ALIAS FOR $1;
	new_template_description ALIAS FOR $2;
	new_template_default_text ALIAS FOR $3;

BEGIN

	RETURN 0;

END;

$$ LANGUAGE 'plpgsql';
