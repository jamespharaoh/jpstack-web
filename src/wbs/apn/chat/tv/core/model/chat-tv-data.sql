---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'chat',
	'tv_to_screen',
	'Send message to screen');

---------------------------------------- INSERT command_type

SELECT command_type_insert (
	'chat',
	'tv_to_screen',
	'Send message to screen');

---------------------------------------- INSERT priv_type

SELECT priv_type_insert (
	'chat',
	'tv_to_screen',
	'Moderate user messages to screen',
	'Allows a user to moderate messages sent by chat users to the TV screen',
	true);

---------------------------------------- INSERT queue_type

SELECT queue_type_insert (
	'chat',
	'tv_to_screen',
	'Send message to screen',
	'chat_user',
	'chat_user');
