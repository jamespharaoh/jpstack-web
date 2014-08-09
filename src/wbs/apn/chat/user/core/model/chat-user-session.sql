CREATE INDEX chat_user_session_chat_user_id
ON chat_user_session (chat_user_id, start_time);

CREATE INDEX chat_user_session_start_time
ON chat_user_session (start_time);
