SELECT object_type_insert (
	'user',
	'user',
	'slice',
	1);

SELECT object_type_insert (
	'user_online',
	'user online',
	'user',
	4);

SELECT object_type_insert (
	'user_session',
	'user session',
	'user',
	3);

SELECT priv_type_insert (
	'user',
	'manage',
	'Manage',
	'Full control',
	true);

SELECT object_type_insert (
	'user_priv',
	'user priv',
	'user',
	4);
