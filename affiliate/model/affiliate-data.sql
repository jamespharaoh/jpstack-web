
-- affiliate type

SELECT object_type_insert (
	'affiliate_type',
	'affiliate type',
	'object_type',
	2);

-- affiliate

SELECT object_type_insert (
	'affiliate',
	'affiliate',
	NULL,
	2);

-- root

SELECT affiliate_type_insert (
	'root',
	'system',
	'System default affiliate');
