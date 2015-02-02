SELECT priv_type_insert (
	'im_chat',
	'manage',
	'Full control',
	'Full control of this IM chat',
	true);

SELECT queue_type_insert (
	'im_chat',
	'reply',
	'Reply',
	'im_chat_conversation',
	'im_chat_message');
