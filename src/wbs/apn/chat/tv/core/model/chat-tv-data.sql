---------------------------------------- INSERT object_type

SELECT object_type_insert (
	'chat_tv',
	'chat tv',
	'chat',
	1);

SELECT object_type_insert (
	'chat_tv_scheme',
	'chat tv scheme',
	'chat_scheme',
	1);

SELECT object_type_insert (
	'chat_tv_user',
	'chat tv user',
	'chat_tv',
	3);

SELECT object_type_insert (
	'chat_tv_user_spend',
	'chat tv user spend',
	'chat_tv_user',
	3);

SELECT object_type_insert (
	'chat_tv_carousel',
	'chat tv carousel',
	'chat_tv',
	3);

SELECT object_type_insert (
	'chat_tv_message',
	'chat tv message',
	'chat_tv',
	3);

SELECT object_type_insert (
	'chat_tv_moderation',
	'chat tv moderation',
	'chat_user',
	2);

SELECT object_type_insert (
	'chat_tv_outbox',
	'chat tv outbox',
	'chat_tv_message',
	4);

SELECT object_type_insert (
	'chat_tv_pic_uploaded',
	'chat tv pic uploaded',
	'media',
	2);

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
