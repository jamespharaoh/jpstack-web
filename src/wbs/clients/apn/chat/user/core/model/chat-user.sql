
CREATE INDEX chat_user_next_adult_ad
ON chat_user (next_adult_ad)
WHERE next_adult_ad IS NOT NULL;

CREATE INDEX chat_user_old_number
ON chat_user (old_number_id);

CREATE INDEX chat_user_next_outbound
ON chat_user (next_outbound)
WHERE next_outbound IS NOT NULL;

CREATE INDEX chat_user_next_join_outbound
ON chat_user (next_join_outbound)
WHERE next_join_outbound IS NOT NULL;

CREATE OR REPLACE FUNCTION chat_user_after_insert ()
RETURNS TRIGGER AS $$

BEGIN

	IF NEW.chat_affiliate_id IS NOT NULL THEN

		UPDATE chat_affiliate
		SET num_users = num_users + 1
		WHERE id = NEW.chat_affiliate_id;

	END IF;

	RETURN NULL;
END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION chat_user_after_update ()
RETURNS TRIGGER AS $$
BEGIN

	IF (OLD.chat_affiliate_id IS NULL
			AND NEW.chat_affiliate_id IS NOT NULL) OR
		(OLD.chat_affiliate_id IS NOT NULL
			AND NEW.chat_affiliate_id IS NULL) OR
		(OLD.chat_affiliate_id != NEW.chat_affiliate_id)
	THEN

		UPDATE chat_affiliate
		SET num_users = num_users - 1
		WHERE id = OLD.chat_affiliate_id;

		UPDATE chat_affiliate
		SET num_users = num_users + 1
		WHERE id = NEW.chat_affiliate_id;

	END IF;

	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION chat_user_after_delete ()
RETURNS TRIGGER AS $$
BEGIN

	IF OLD.chat_affiliate_id IS NOT NULL THEN

		UPDATE chat_affiliate
		SET num_users = num_users - 1
		WHERE id = OLD.chat_affiliate_id;

	END IF;

	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER chat_user_after_insert
AFTER INSERT ON chat_user
FOR EACH ROW EXECUTE PROCEDURE chat_user_after_insert ();

CREATE TRIGGER chat_user_after_update
AFTER UPDATE ON chat_user
FOR EACH ROW EXECUTE PROCEDURE chat_user_after_update ();

CREATE TRIGGER chat_user_after_delete
AFTER DELETE ON chat_user
FOR EACH ROW EXECUTE PROCEDURE chat_user_after_delete ();

---------------------------------------- TABLE chat_user_alarm

CREATE INDEX chat_user_alarm_secondary_index
ON chat_user_alarm (chat_user_id, monitor_chat_user_id);

CREATE INDEX chat_user_alarm_time
ON chat_user_alarm (alarm_time);
