
-- chat affiliate

SELECT object_type_insert (
	'chat_affiliate',
	'chat affiliate',
	'chat_scheme',
	1);

SELECT priv_type_insert (
	'chat_affiliate',
	'manage',
	'Full control',
	'Full control of a chat affiliate',
	true);

SELECT priv_type_insert (
	'chat_affiliate',
	'messages',
	'View messages',
	'View messages for a chat affiliate',
	true);

SELECT priv_type_insert (
	'chat_affiliate',
	'stats',
	'View stats',
	'View messages stats for a chat affiliate',
	true);

SELECT affiliate_type_insert (
	'chat_affiliate',
	'default',
	'Chat affiliate');

-- chat scheme

SELECT priv_type_insert (
	'chat_scheme',
	'affiliate_create',
	'Create chat affiliates',
	'Create chat affiliates in a chat scheme',
	true);
