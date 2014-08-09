
CREATE INDEX chat_broadcast_recent
ON chat_broadcast (chat_id, timestamp DESC);

-- chat broadcast number

CREATE TABLE chat_broadcast_number (
	chat_broadcast_id int NOT NULL REFERENCES chat_broadcast,
	number_id int NOT NULL REFERENCES number,
	PRIMARY KEY (chat_broadcast_id, number_id)
);
