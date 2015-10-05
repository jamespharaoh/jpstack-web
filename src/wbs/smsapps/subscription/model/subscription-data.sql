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
