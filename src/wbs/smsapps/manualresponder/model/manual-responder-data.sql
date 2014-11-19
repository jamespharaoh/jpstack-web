
-- manual responder

SELECT object_type_insert (
	'manual_responder',
	'manual responder',
	'slice',
	1);

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

-- manual responder template

SELECT object_type_insert (
	'manual_responder_template',
	'manual responder template',
	'manual_responder',
	4);

-- manual responder request

SELECT object_type_insert (
	'manual_responder_request',
	'manual responder request',
	'manual_responder',
	3);

SELECT object_type_insert (
	'manual_responder_report',
	'manual responder report',
	'manual_responder_request',
	3);

SELECT queue_type_insert (
	'manual_responder',
	'default',
	'Manual responder',
	'number',
	'manual_responder_request');

-- manual responder reply

SELECT object_type_insert (
	'manual_responder_reply',
	'manual responder reply',
	'manual_responder_request',
	3);

-- manual responder number

SELECT object_type_insert (
	'manual_responder_number',
	'manual responder number',
	'manual_responder',
	3);

-- slice

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
