
SELECT priv_type_insert (
	'manual_responder',
	'manage',
	'Full control',
	'Full control over the manual responder',
	true);

SELECT priv_type_insert (
	'manual_responder',
	'supervisor',
	'Supervisor',
	'View supervisor reports for this manual responder',
	true);

SELECT priv_type_insert (
	'manual_responder',
	'reply',
	'Reply to requests',
	'Reply to requests received by the manual responder',
	true);

SELECT priv_type_insert (
	'manual_responder',
	'stats',
	'View stats',
	'View message stats for the manual responder',
	true);

SELECT priv_type_insert (
	'manual_responder',
	'messages',
	'View message history',
	'View message history for the manual responder',
	true);

SELECT command_type_insert (
	'manual_responder',
	'default',
	'Manual responder');

SELECT service_type_insert (
	'manual_responder',
	'default',
	'Manual responder');

SELECT queue_type_insert (
	'manual_responder',
	'default',
	'Manual responder',
	'number',
	'manual_responder_request');

SELECT priv_type_insert (
	'slice',
	'manual_responder_create',
	'Create manual responders',
	'Create new manual responders in this slice',
	true);

SELECT priv_type_insert (
	'manual_responder',
	'number',
	'View phone numbers',
	'View phone numbers for this manual responder',
	true);

SELECT delivery_type_insert (
	'manual_responder',
	'default');
