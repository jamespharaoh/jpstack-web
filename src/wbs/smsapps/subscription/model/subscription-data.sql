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

SELECT service_type_insert (
	'subscription',
	'default',
	'Default');

SELECT service_type_insert (
	'subscription_list',
	'default',
	'Default');

SELECT priv_type_insert (
	'subscription',
	'manage',
	'Full control',
	'Full control of this subscription',
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

SELECT batch_type_insert (
	'subscription',
	'send',
	'Subscription send',
	'subscription_send');

SELECT delivery_type_insert (
	'subscription',
	'Subscription');

SELECT affiliate_type_insert (
	'subscription_affiliate',
	'default',
	'Default');

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

SELECT event_type_insert (
	'subscription_number_affiliate',
	'Assign %0 to %1 due to %2');

SELECT event_type_insert (
	'subscription_number_subscribe',
	'Subscribed %0 to %1 due to %2');
