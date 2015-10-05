SELECT event_type_insert (
	'broadcast_numbers_added',
	'%0 added %1 numbers to %2');

SELECT event_type_insert (
	'broadcast_numbers_removed',
	'%0 removed %1 numbers from %2');

SELECT event_type_insert (
	'broadcast_scheduled',
	'%0 scheduled %1 for %2');

SELECT event_type_insert (
	'broadcast_unscheduled',
	'%0 unscheduled %1');

SELECT event_type_insert (
	'broadcast_cancelled',
	'%0 cancelled %1 for %2');

SELECT event_type_insert (
	'broadcast_send_begun',
	'%0 begun sending');

SELECT event_type_insert (
	'broadcast_send_completed',
	'%0 completed sending');

SELECT batch_type_insert (
	'broadcast_config',
	'broadcast',
	'Broadcast',
	'broadcast');
