SELECT command_type_insert (
	'forwarder',
	'default',
	'Forwarder');

SELECT delivery_type_insert (
	'forwarder',
	'Forwarder');

SELECT message_set_type_insert (
	'forwarder',
	'default',
	'Forwarder');

SELECT priv_type_insert (
	'forwarder',
	'manage',
	'Full control',
	'',
	true);

SELECT priv_type_insert (
	'forwarder',
	'stats',
	'View message stats',
	'',
	true);

SELECT priv_type_insert (
	'forwarder',
	'messages',
	'View message history',
	'',
	true);

SELECT priv_type_insert (
	'slice',
	'forwarder_create',
	'Create new forwarders',
	'',
	true);

SELECT service_type_insert (
	'forwarder',
	'default',
	'Forwarder');
