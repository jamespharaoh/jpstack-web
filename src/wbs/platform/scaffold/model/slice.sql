---------------------------------------- TABLE slice

SELECT object_type_insert (
	'slice',
	'slice',
	'root',
	1);

SELECT priv_type_insert (
	'slice',
	'manage',
	'Full control',
	'Full control over this slice and all objects in it',
	true);

SELECT priv_type_insert (
	'slice',
	'supervisor',
	'Supervisor',
	'View supervisor reports for all items in this slice',
	true);

CREATE OR REPLACE FUNCTION slice_id (text) RETURNS int AS '
	SELECT id FROM slice WHERE code = $1;
' LANGUAGE SQL;
