SELECT priv_type_insert (
	'im_chat',
	'manage',
	'Full control',
	'Full control of this IM chat',
	true);

SELECT priv_type_insert (
	'im_chat',
	'stats',
	'View stats',
	'View messages stats for this IM chat',
	true);

SELECT priv_type_insert (
	'im_chat',
	'messages',
	'View messages',
	'View message history for this IM chat',
	true);

SELECT priv_type_insert (
	'im_chat',
	'supervisor',
	'Supervisor',
	'View supervisor information for this IM chat',
	true);

SELECT queue_type_insert (
	'im_chat',
	'reply',
	'Reply',
	'im_chat_conversation',
	'im_chat_message');

SELECT queue_type_insert (
	'im_chat',
	'pending',
	'Pending',
	'im_chat_conversation',
	'im_chat_message');
