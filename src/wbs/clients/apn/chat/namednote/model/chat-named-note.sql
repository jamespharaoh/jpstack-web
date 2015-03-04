CREATE INDEX chat_named_note_secondary_key
ON chat_named_note (this_user_id, other_user_id, chat_note_name_id);
