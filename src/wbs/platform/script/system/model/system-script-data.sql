
-- system script

SELECT object_type_insert (
	'system_script',
	'system script',
	'root',
	1);

SELECT priv_type_insert (
	'system_script',
	'manage',
	'Manage',
	'Full control',
	true);

SELECT priv_type_insert (
	'system_script',
	'system_script_run',
	'Run',
	'Run a system script',
	true);

-- system script revision

SELECT object_type_insert (
	'system_script_revision',
	'system script revision',
	'system_script',
	3);
