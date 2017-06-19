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
	chat_message.chat_id AS chat_id,
	chat_message.sender_user_id AS sender_user_id,
	timestamp AS timestamp,

	1 AS num_messages,
	(to_chat_user.type = 'm')::int AS num_messages_in,
	(from_chat_user.type = 'm')::int AS num_messages_out,

	length (original_text.text) AS num_characters,

	CASE WHEN to_chat_user.type = 'm'
	THEN length (original_text.text) ELSE 0
	END AS num_characters_in,

	CASE WHEN from_chat_user.type = 'm'
	THEN length (original_text.text) ELSE 0
	END AS num_characters_out,

	CASE WHEN chat_message.final
	THEN 1 ELSE 0
	END AS num_messages_final,

	CASE WHEN chat_message.final AND to_chat_user.type = 'm'
	THEN 1 ELSE 0
	END AS num_messages_final_in,

	CASE WHEN chat_message.final AND from_chat_user.type = 'm'
	THEN 1 ELSE 0
	END AS num_messages_final_out

FROM chat_message

INNER JOIN chat_user AS to_chat_user
	ON chat_message.to_user_id
		= to_chat_user.id

INNER JOIN chat_user AS from_chat_user
	ON chat_message.from_user_id
		= from_chat_user.id

INNER JOIN text AS original_text
	ON chat_message.original_text_id
		= original_text.id

;
