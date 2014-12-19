SELECT priv_type_insert (
	'orderer',
	'manage',
	'Full control',
	'Full control of this orderer',
	true);

SELECT priv_type_insert (
	'orderer',
	'messages',
	'Message history',
	'View message history for this orderer',
	true);

SELECT priv_type_insert (
	'orderer',
	'stats',
	'Messages stast',
	'View message stats for this orderer',
	true);

SELECT priv_type_insert (
	'slice',
	'orderer_list',
	'List orderers',
	'List orderers in this slice',
	true);

SELECT priv_type_insert (
	'slice',
	'orderer_create',
	'Create orderer',
	'Create new orderers in this slice',
	true);

---------------------------------------- INSERT command_type

SELECT command_type_insert (
	'orderer',
	'default',
	'Default');

---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'orderer',
	'default',
	'Default');

---------------------------------------- INSERT delivery_type

SELECT delivery_type_insert (
	'orderer',
	'Orderer');
