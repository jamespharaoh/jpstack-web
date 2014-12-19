SELECT priv_type_insert (
	'auto_responder',
	'manage',
	'Full control',
	'',
	true);

SELECT priv_type_insert (
	'auto_responder',
	'messages',
	'View messages',
	'',
	true);

SELECT priv_type_insert (
	'auto_responder',
	'stats',
	'View stats',
	'',
	true);

SELECT priv_type_insert (
	'slice',
	'auto_responder_create',
	'Create new auto responders',
	'',
	true);

SELECT service_type_insert (
	'auto_responder',
	'default',
	'Auto responders messages');

SELECT message_set_type_insert (
	'auto_responder',
	'default',
	'Auto responder message set');

SELECT command_type_insert (
	'auto_responder',
	'default',
	'Auto responder command');
