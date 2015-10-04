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

SELECT delivery_type_insert (
	'manual_responder',
	'default');
