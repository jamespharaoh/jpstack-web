SELECT psychic_template_type_insert (
	'charges',
	'Confirm charges',
	'Respond to this message with the word YES to confirm you accept the '
	|| 'charges for using this service');

SELECT psychic_template_type_insert (
	'welcome',
	'Welcome message',
	'Welcome to the service. Text KEYWORD to SHORTCODE for the next psychic '
	|| 'profile. Reply to this message for help. Reply to a psychic profile to '
	|| 'ask a question. Reply to their response to ask another.');

SELECT psychic_template_type_insert (
	'daily_limit_reached',
	'Barred due to daily bill limit being reached',
	'You have reached the daily bill limit enforced by your operator. You will '
	|| 'need to wait until tomorrow until we can bill you correctly to resume '
	|| 'use of the service.');

SELECT psychic_template_type_insert (
	'credit_limit_reached',
	'Barred due to credit limit being reached',
	'We are having trouble billing you. Please ensure you have credit or '
	|| 'contact us for more information.');

SELECT psychic_template_type_insert (
	'prepay_credit_exhausted',
	'Barred to to no credit for prepay user',
	'Your credit is now at zero. Please contact us to purchase more. Reply to '
	|| 'this message for help.');

SELECT psychic_template_type_insert (
	'profile',
	'Next psychic profile',
	'{name}: {info}');

SELECT psychic_template_type_insert (
	'response_single',
	'Response from psychic (single message)',
	'{name}: {message}');

SELECT psychic_template_type_insert (
	'response_first',
	'Response from psychic (first of multipart message)',
	'{name} {page}/{pages}: {message}');

SELECT psychic_template_type_insert (
	'response_middle',
	'Response from psychic (middle of multipart message)',
	'{name} {page}/{pages}: {message}');

SELECT psychic_template_type_insert (
	'response_last',
	'Response from psychic (last of multipart message)',
	'{name} {page}/{pages}: {message}');

SELECT psychic_template_type_insert (
	'bill',
	'Billed message',
	'This is a billed message. Thank you for using this service.');

SELECT psychic_template_type_insert (
	'stop',
	'Service stopped',
	'All messages from this service have been stopped. You can reactivate at '
	|| 'any time by replying to any message.');
