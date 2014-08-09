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
