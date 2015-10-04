SELECT event_type_insert (
	'alerts_number_created',
	'%0 added number id %1 to %2');

SELECT event_type_insert (
	'alerts_number_deleted',
	'%0 deleted number id %1 from %2');

SELECT event_type_insert (
	'alerts_number_updated',
	'%0 set %1 for number id %2 of %3 to %4');
