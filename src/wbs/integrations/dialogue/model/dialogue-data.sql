
SELECT object_type_insert (
	'dialogue_mms_route',
	'dialogue mms route',
	'route',
	2);

SELECT object_type_insert (
	'dialogue_locator',
	'dialogue locator',
	'root',
	1);

--SELECT locator_type_insert (
--	'dialogue_locator',
--	'default',
--	'Dialogue locator');

SELECT sender_insert (
	'dialogue_mms',
	'Dialogue MMS');
