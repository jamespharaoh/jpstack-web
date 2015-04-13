CREATE INDEX chat_broadcast_recent
ON chat_broadcast (chat_id, created_time DESC);
