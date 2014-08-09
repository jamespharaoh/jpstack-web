
-- object types

SELECT object_type_insert (
	'chat_user_spend',
	'chat user spend',
	'chat_user',
	3);

SELECT object_type_insert (
	'chat_user_credit',
	'chat user credit',
	'chat_user',
	3);

SELECT object_type_insert (
	'chat_daily_limit_log',
	'chat daily limit log',
	'chat_user',
	3);

SELECT object_type_insert (
	'chat_user_bill_log',
	'chat user bill log',
	'chat_user',
	3);

SELECT object_type_insert (
	'chat_user_credit_limit_log',
	'chat user credit limit log',
	'chat_user',
	3);

SELECT object_type_insert (
	'chat_network',
	'chat network',
	'chat',
	4);

SELECT command_type_insert (
	'chat',
	'check_credit',
	'Request credit balance');

-- service type

SELECT service_type_insert (
	'chat',
	'bill',
	'Billed messages');
