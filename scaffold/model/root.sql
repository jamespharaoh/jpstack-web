---------------------------------------- INSERT object_type

INSERT INTO object_type (
	id,
	code,
	description,
	parent_object_type_id,
	stature)
VALUES (
	0,
	'root',
	'root',
	null,
	1);

---------------------------------------- INSERT priv_type

SELECT priv_type_insert (
	'root',
	'alert',
	'See alerts',
	'See alert notifications in the status bar',
	true);

SELECT priv_type_insert (
	'root',
	'manage',
	'Full control',
	'Full control over the entire system',
	true);
