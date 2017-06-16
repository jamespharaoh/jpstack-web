CREATE UNIQUE INDEX im_chat_customer_email
ON im_chat_customer (im_chat_id, email);

CREATE UNIQUE INDEX im_chat_session_secret
ON im_chat_session (secret);

CREATE UNIQUE INDEX im_chat_purchase_token
ON im_chat_purchase (token);

CREATE INDEX im_chat_conversation_email_pending
ON im_chat_conversation (id)
WHERE end_time IS NOT NULL and email_time IS NULL;

---------- VIEW im_chat_message_view

CREATE OR REPLACE VIEW im_chat_message_view
AS SELECT

	im_chat_message.id as id,
	im_chat.id AS im_chat_id,
	sender_user.id AS sender_user_id,
	timestamp AS timestamp,

	length (im_chat_message.message_text) as num_characters

FROM im_chat_message

INNER JOIN im_chat_conversation
	ON im_chat_message.im_chat_conversation_id
		= im_chat_conversation.id

INNER JOIN im_chat_customer
	ON im_chat_conversation.im_chat_customer_id
		= im_chat_customer.id

INNER JOIN im_chat
	ON im_chat_customer.im_chat_id
		= im_chat.id

INNER JOIN "user" AS sender_user
	ON im_chat_message.sender_user_id
		= sender_user.id

;
