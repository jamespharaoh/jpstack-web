------------------------------------------------------------ TABLE object_type

CREATE OR REPLACE FUNCTION object_type_id (text)
RETURNS int AS $$

	SELECT id
	FROM object_type
	WHERE code = $1;

$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION object_type_code (int)
RETURNS text AS $$

	SELECT code
	FROM object_type
	WHERE id = $1;

$$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION object_type_insert (text, text, text, int)
RETURNS int AS '

  DECLARE

    new_code ALIAS FOR $1;
    new_description ALIAS FOR $2;
    the_parent_object_type_code ALIAS FOR $3;
    new_stature ALIAS FOR $4;

    the_parent_object_type_id int;
    new_id int;

  BEGIN

    IF the_parent_object_type_code IS NOT NULL THEN
      SELECT INTO the_parent_object_type_id id
        FROM object_type
        WHERE code = the_parent_object_type_code;
      IF NOT FOUND THEN
        RAISE EXCEPTION ''Object type not found: %'', the_parent_object_type_code;
      END IF;
    END IF;

    SELECT INTO new_id id FROM object_type WHERE code = new_code;
    IF FOUND THEN
      UPDATE object_type
        SET description = new_description,
          parent_object_type_id = the_parent_object_type_id,
          stature = new_stature
         WHERE id = new_id;
      RETURN new_id;
    END IF;

    new_id := nextval (''object_type_id_seq'');
    INSERT INTO object_type (id, code, description, parent_object_type_id, stature)
      VALUES (new_id, new_code, new_description, the_parent_object_type_id, new_stature);
    RETURN new_id;

  END;
' LANGUAGE 'plpgsql';
