CREATE UNIQUE INDEX im_chat_customer_email
ON im_chat_customer (im_chat_id, email);

CREATE UNIQUE INDEX im_chat_session_secret
ON im_chat_session (secret);

CREATE UNIQUE INDEX im_chat_purchase_token
ON im_chat_purchase (token);

CREATE INDEX im_chat_conversation_email_pending
ON im_chat_conversation (id)
WHERE end_time IS NOT NULL and email_time IS NULL;