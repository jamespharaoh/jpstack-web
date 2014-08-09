-- service type

SELECT object_type_insert (
	'service_type',
	'service type',
	'object_type',
	2);

-- service

SELECT object_type_insert (
	'service',
	'service',
	NULL,
	2);

-- root

SELECT service_type_insert (
	'root',
	'system',
	'System default service');
