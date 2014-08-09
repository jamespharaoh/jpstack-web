
SELECT object_type_insert (
	'chat',
	'chat',
	'slice',
	1);

SELECT object_type_insert (
	'chat_stats',
	'chat stats',
	'chat',
	3);

SELECT object_type_insert (
	'chat_place',
	'chat place',
	'chat',
	4);

SELECT object_type_insert (
	'chat_place_name',
	'chat place name',
	'chat_place',
	4);

SELECT priv_type_insert (
	'chat',
	'supervisor',
	'Supervisor',
	'View supervisor reports',
	true);

SELECT object_type_insert (
	'chat_scheme',
	'chat scheme',
	'chat',
	1);

SELECT object_type_insert (
	'chat_scheme_charges',
	'chat scheme charges',
	'chat_scheme',
	2);

SELECT object_type_insert (
	'chat_scheme_keyword',
	'chat scheme keyword',
	'chat_scheme',
	4);

SELECT object_type_insert (
	'chat_user',
	'chat user',
	'chat',
	3);

SELECT object_type_insert (
	'chat_contact',
	'chat contact',
	'chat',
	3);

SELECT object_type_insert (
	'chat_contact_note',
	'chat contact note',
	'chat_contact',
	4);

SELECT object_type_insert (
	'chat_message',
	'chat message',
	'chat',
	3);

SELECT object_type_insert (
	'chat_help_log',
	'chat help log',
	'chat_user',
	3);

SELECT object_type_insert (
	'chat_help_template',
	'chat help template',
	'chat',
	4);

SELECT object_type_insert (
	'chat_monitor_inbox',
	'chat monitor inbox',
	'chat_contact',
	4);

SELECT object_type_insert (
	'chat_keyword',
	'chat keyword',
	'chat',
	4);

---------------------------------------- INSERT priv_type

SELECT priv_type_insert (
	'chat',
	'broadcast',
	'View and send broadcasts',
	'Allows a user to create new broadcast advertising messages, and to view '
	|| 'the history of previous messages.',
	true);

SELECT priv_type_insert (
	'chat_scheme',
	'stats',
	'View stats',
	'View messages stats for a chat scheme',
	true);

SELECT priv_type_insert (
	'chat_scheme',
	'messages',
	'View messages',
	'View message history for a chat scheme',
	true);

---------------------------------------- INSERT batch_type

SELECT batch_type_insert (
	'chat',
	'broadcast',
	'Broadcast',
	'chat');

---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'chat',
	'broadcast',
	'Broadcast messages to large numbers of users');

---------------------------------------- INSERT event_type

SELECT event_type_insert (
	'chat_user_auto_strict',
	'%0 was automatically moved from normal to strict credit mode due to a '
	|| 'high rate of failure.');

SELECT event_type_insert (
	'chat_user_monitor_created',
	'%0 created %1 with name %2, place %3, gender %4, orient %5 and info %6');

SELECT event_type_insert (
	'chat_user_barred',
	'%0 barred %1 for reason: %2');

SELECT event_type_insert (
	'chat_user_unbarred',
	'%0 removed bar on %1 for reason: %2');

SELECT event_type_insert (
	'chat_user_info_admin',
	'%0 changed info for %1 from %2 to %3 for reason %4');

SELECT event_type_insert (
	'chat_user_info_cleared',
	'%0 cleared info for %1, was %2, for reason %3');

SELECT event_type_insert (
	'chat_user_prefs',
	'%0 set prefs for %1 to gender %2 and orient %3');

SELECT event_type_insert (
	'chat_user_credit_mode',
	'%0 changed credit mode for %1 from %2 to %3');

SELECT event_type_insert (
	'chat_user_delete',
	'%0 deleted %1');

SELECT event_type_insert (
	'chat_user_undelete',
	'%0 undeleted %1');

SELECT event_type_insert (
	'chat_user_imageMode',
	'%0 set image mode for %1 to %2');

SELECT event_type_insert (
	'chat_user_online',
	'%0 brought %1 online');

SELECT event_type_insert (
	'chat_user_offline',
	'%0 took %1 offline');

---------------------------------------- INSERT delivery_type

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

-- chat scheme

SELECT affiliate_type_insert (
	'chat_scheme',
	'default',
	'Chat scheme');

---------------------------------------- INSERT service

SELECT service_type_insert (
	'chat',
	'system',
	'System message');

SELECT number_lookup_type_insert (
	'chat',
	'block_all',
	'Numbers which have requested an end to all services');
