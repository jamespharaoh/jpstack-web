
SELECT object_type_insert (
	'forwarder',
	'forwarder',
	'slice',
	1);

SELECT object_type_insert (
	'forwarder_route',
	'forwarder route',
	'forwarder',
	1);

SELECT object_type_insert (
	'forwarder_message_out',
	'forwarder message out',
	'forwarder',
	3);

SELECT object_type_insert (
	'forwarder_message_out_report',
	'forwarder message out report',
	'forwarder_message_out',
	3);

SELECT object_type_insert (
	'forwarder_message_in',
	'forwarder message in',
	'forwarder',
	3);

SELECT command_type_insert (
	'forwarder',
	'default',
	'Forwarder');

---------------------------------------- INSERT delivery_notice_type

SELECT delivery_type_insert (
	'forwarder',
	'Forwarder');

---------------------------------------- INSERT message_set_type

SELECT message_set_type_insert (
	'forwarder',
	'default',
	'Forwarder');

---------------------------------------- INSERT priv_type

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

---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'forwarder',
	'default',
	'Forwarder');
