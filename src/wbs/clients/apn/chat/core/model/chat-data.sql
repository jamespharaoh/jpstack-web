SELECT batch_type_insert (
	'chat',
	'broadcast',
	'Broadcast',
	'chat');

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

SELECT event_type_insert (
	'chat_broadcast_send_completed',
	'%0 completed sending');

SELECT event_type_insert (
	'chat_user_location_api',
	'Location for %0 set to (%1,%2) via API');

SELECT event_type_insert (
	'chat_user_place_message',
	'Location for %0 set to %1 (%2,%3) by %4');

SELECT event_type_insert (
	'chat_user_place_user',
	'Location for %0 set to %1 (%2,%3) by %4');

SELECT event_type_insert (
	'chat_user_place_api',
	'Location for %0 set to %1 (%2,%3) by API');

SELECT event_type_insert (
	'chat_user_location_locator',
	'Location for %0 set to (%1,%2) via %3');
