SELECT batch_type_insert (
	'chat',
	'broadcast',
	'Broadcast',
	'chat');

SELECT delivery_type_insert (
	'chat_bill',
	'Chat bill');

SELECT delivery_type_insert (
	'chat_bill_strict',
	'Chat bill strict');

SELECT delivery_type_insert (
	'chat_adult',
	'Chat adult verification');

SELECT delivery_type_insert (
	'chat_adult_join',
	'Chat adult verification and join');

SELECT affiliate_type_insert (
	'chat_scheme',
	'default',
	'Chat scheme');

SELECT number_lookup_type_insert (
	'chat',
	'block_all',
	'Numbers which have requested an end to all services');
