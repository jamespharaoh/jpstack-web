---------------------------------------- INSERT object_type

SELECT object_type_insert (
	'subscription',
	'subscription',
	'slice',
	1);

SELECT object_type_insert (
	'subscription_keyword',
	'subscription keyword',
	'subscription',
	1);

SELECT object_type_insert (
	'subscription_list',
	'subscription list',
	'subscription',
	1);

SELECT object_type_insert (
	'subscription_affiliate',
	'subscription affiliate',
	'subscription',
	1);

SELECT object_type_insert (
	'subscription_number',
	'subscription number',
	'subscription',
	3);

SELECT object_type_insert (
	'subscription_send',
	'subscription send',
	'subscription',
	3);

SELECT object_type_insert (
	'subscription_send_part',
	'subscription send part',
	'subscription_send',
	2);

SELECT object_type_insert (
	'subscription_send_number',
	'subscription send number',
	'subscription_send',
	3);

SELECT object_type_insert (
	'subscription_bill',
	'subscription bill',
	'subscription_number',
	3);

---------------------------------------- INSERT command_type

SELECT command_type_insert (
	'subscription',
	'subscribe',
	'Subscribe');

SELECT command_type_insert (
	'subscription',
	'unsubscribe',
	'Unsubscribe');

SELECT command_type_insert (
	'subscription_affiliate',
	'subscribe',
	'Subscribe');

SELECT command_type_insert (
	'subscription_keyword',
	'subscribe',
	'Subscribe');

SELECT command_type_insert (
	'subscription_list',
	'subscribe',
	'Subscribe');

---------------------------------------- INSERT service_type

SELECT service_type_insert (
	'subscription',
	'default',
	'Default');

SELECT service_type_insert (
	'subscription_list',
	'default',
	'Default');

---------------------------------------- INSERT messageset_type

SELECT message_set_type_insert (
	'subscription',
	'subscribe_success',
	'Subscribe successful');

SELECT message_set_type_insert (
	'subscription',
	'subscribe_already',
	'Subscription failed because already subscribed');

SELECT message_set_type_insert (
	'subscription',
	'unsubscribe_success',
	'Unsubscribe successful');

SELECT message_set_type_insert (
	'subscription',
	'unsubscribe_already',
	'Unsubscription failed because already unsubscribed');

---------------------------------------- INSERT priv_type

SELECT priv_type_insert (
	'subscription',
	'manage',
	'Full control',
	'Full control of this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'admin',
	'Admin tasks',
	'Perform administrative tasks on this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'subscription_send',
	'Send to subscription',
	'Send messages to users who are subscribed to this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'stats',
	'View message stats',
	'View message stats for this subscription',
	true);

SELECT priv_type_insert (
	'subscription',
	'messages',
	'View message history',
	'View message history for this subscription',
	true);

SELECT priv_type_insert (
	'subscription_list',
	'manage',
	'Full control',
	'Full control of this subscription list',
	true);

SELECT priv_type_insert (
	'subscription_list',
	'stats',
	'View message stats',
	'View message stats for this subscription list',
	true);

SELECT priv_type_insert (
	'subscription_list',
	'messages',
	'View message history',
	'View message history for this subscription list',
	true);

SELECT priv_type_insert (
	'subscription_affiliate',
	'admin',
	'Admin tasks',
	'Perform administrative tasks on this subscription affiliage',
	true);

SELECT priv_type_insert (
	'subscription_affiliate',
	'stats',
	'View message stats',
	'View message stats for this subscription affiliate',
	true);

SELECT priv_type_insert (
	'subscription_affiliate',
	'messages',
	'View message history',
	'View message history for this subscription affiliate',
	true);

SELECT priv_type_insert (
	'slice',
	'subscription_create',
	'Create subscription',
	'Create subscriptions in this slice',
	true);

SELECT priv_type_insert (
	'slice',
	'subscription_list',
	'List subscriptions',
	'List all subscriptions in this slice',
	true);

---------------------------------------- INSERT batch_type

SELECT batch_type_insert (
	'subscription',
	'send',
	'Subscription send',
	'subscription_send');

--------------------------------------- INSERT delivery_type

SELECT delivery_type_insert (
	'subscription',
	'Subscription');

---------------------------------------- INSERT affiliate_type

SELECT affiliate_type_insert (
	'subscription_affiliate',
	'default',
	'Default');

---------------------------------------- INSERT event_type

SELECT event_type_insert (
	'subscription_send_scheduled',
	'%0 scheduled %1 for %2');

SELECT event_type_insert (
	'subscription_send_unscheduled',
	'%0 unscheduled %1');

SELECT event_type_insert (
	'subscription_send_cancelled',
	'%0 cancelled %1 for %2');

SELECT event_type_insert (
	'subscription_send_begun',
	'%0 begun sending');

SELECT event_type_insert (
	'subscription_send_completed',
	'%0 completed sending');
