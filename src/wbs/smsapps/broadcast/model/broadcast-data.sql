
-- broadcast config

SELECT object_type_insert (
	'broadcast_config',
	'broadcast config',
	'slice',
	1);

SELECT priv_type_insert (
	'broadcast_config',
	'manage',
	'Full control',
	'Full control of the broadcast config',
	true);

SELECT priv_type_insert (
	'broadcast_config',
	'messages',
	'View messages',
	'View message history for the broadcast config',
	true);

SELECT priv_type_insert (
	'broadcast_config',
	'stats',
	'Message stats',
	'View message stats for the broadcast config',
	true);

SELECT service_type_insert (
	'broadcast_config',
	'default',
	'Default');

-- broadcast

SELECT object_type_insert (
	'broadcast',
	'broadcast',
	'broadcast_config',
	3);

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

-- broadcast number

SELECT object_type_insert (
	'broadcast_number',
	'broadcast number',
	'broadcast',
	3);

-- batch

SELECT batch_type_insert (
	'broadcast_config',
	'broadcast',
	'Broadcast',
	'broadcast');
