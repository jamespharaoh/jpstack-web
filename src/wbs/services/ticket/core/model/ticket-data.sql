SELECT priv_type_insert (
	'ticket_manager',
	'manage',
	'Full control',
	'Full control of this Ticket Manager',
	true);

SELECT queue_type_insert (
	'ticket_state',
	'default',
	'Default',
	'ticket',
	'ticket');
