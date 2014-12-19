SELECT priv_type_insert (
	'ticketer',
	'manage',
	'Full control',
	'Full control of this ticketer',
	true);

SELECT priv_type_insert (
	'ticketer',
	'stats',
	'View stats',
	'View message stats for this ticketer',
	true);

SELECT priv_type_insert (
	'ticketer',
	'messages',
	'View messages',
	'View message history for this ticketer',
	true);

SELECT priv_type_insert (
	'slice',
	'ticketer_create',
	'Create ticketer',
	'Create a new ticketer in this slice',
	true);

SELECT priv_type_insert (
	'slice',
	'ticketer_list',
	'List ticketers',
	'List all ticketers in this slice',
	true);

---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'ticketer',
	'default',
	'Default');

---------------------------------------- INSERT command_type

SELECT command_type_insert (
	'ticketer',
	'default',
	'Default');
