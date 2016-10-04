CREATE TABLE chat_rebill_chat_user (
	chat_rebill_log_id int NOT NULL REFERENCES chat_rebill_log,
	chat_user_id int NOT NULL REFERENCES chat_user,
	PRIMARY KEY (chat_rebill_log_id, chat_user_id)
);

-- ex: noet ts=4 filetype=sql