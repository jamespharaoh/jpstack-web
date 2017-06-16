CREATE INDEX chat_message_signup
ON chat_message (chat_id, timestamp)
WHERE status = 8;

--ALTER TABLE chat_user
--ADD CONSTRAINT chat_user_last_message_poll_id_fkey
--FOREIGN KEY (last_message_poll_id)
--REFERENCES chat_message (id);

CREATE TABLE chat_message_media (

	chat_message_id int NOT NULL REFERENCES chat_message,
	index int NOT NULL,
	PRIMARY KEY (chat_message_id, index),

	media_id int NOT NULL REFERENCES media

);

---------- VIEW chat_message_view

CREATE OR REPLACE VIEW chat_message_view
AS SELECT

	chat_message.id as id,
	chat.id AS chat_id,
	sender_user.id AS sender_user_id,
	timestamp AS timestamp,

	length (edited_text.text) as num_characters,
	chat_message.final as final

FROM chat_message

INNER JOIN chat
	ON chat_message.chat_id
		= chat.id

INNER JOIN "user" AS sender_user
	ON chat_message.sender_user_id
		= sender_user.id

INNER JOIN text AS edited_text
	ON chat_message.edited_text_id
		= edited_text.id

;
