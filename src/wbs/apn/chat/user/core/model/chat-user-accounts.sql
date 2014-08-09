SELECT $$

-- (re)create fix_me

DROP TABLE IF EXISTS fix_me;

CREATE TABLE fix_me AS

SELECT *
FROM chat_user_accounts
WHERE
	last_action >= now () - '1 year'::interval
	AND (
		credit_success != message_success
		OR credit_pending != message_pending
		OR credit_failed != message_failed
		OR message_invalid > 0
	);

-- fix users in fix_me table

BEGIN;

UPDATE chat_user
SET credit_success =
		+ chat_user.credit_success
		+ fix_me.message_success
		- fix_me.message_success,
	credit_pending_strict =
		+ chat_user.credit_pending_strict
		+ fix_me.message_pending
		- fix_me.message_pending,
	credit_retried =
		+ chat_user.credit_retried
		+ fix_me.message_failed
		- fix_me.credit_failed
FROM fix_me
WHERE chat_user.id = fix_me.chat_user_id;

ROLLBACK;

-- clean up

DROP TABLE fix_me;

$$;

---------------------------------------- VIEW chat user accounts

CREATE OR REPLACE VIEW chat_user_accounts
AS SELECT

	chat_user.id AS chat_user_id,
	chat_user.value_since_ever AS total_spend,
	chat_user.last_action AS last_action,

	chat_user.credit_success AS credit_success,
	chat_user.credit_pending_strict AS credit_pending,
	chat_user.credit_revoked + chat_user.credit_retried AS credit_failed,

	COALESCE ((
		SELECT sum (message.charge)
		FROM message
		WHERE message.number_id = chat_user.number_id
			AND message.direction = 1
			AND message.charge > 0
			AND message.service_id IN (
				SELECT id
				FROM service
				WHERE service.parent_object_type_id = object_type_id ('chat')
					AND service.parent_object_id = chat.id
			)
			AND message.status IN (5)
			AND message.delivery_type_id IN (
				delivery_type_id ('chat_bill'),
				delivery_type_id ('chat_bill_strict')
			)
	), 0) AS message_success,

	COALESCE ((
		SELECT sum (message.charge)
		FROM message
		WHERE message.number_id = chat_user.number_id
			AND message.direction = 1
			AND message.charge > 0
			AND message.service_id IN (
				SELECT id
				FROM service
				WHERE service.parent_object_type_id = object_type_id ('chat')
					AND service.parent_object_id = chat.id
			)
			AND message.status IN (0, 4, 10)
			AND message.delivery_type_id = delivery_type_id ('chat_bill_strict')
	), 0) AS message_pending,

	COALESCE ((
		SELECT sum (message.charge)
		FROM message
		WHERE message.number_id = chat_user.number_id
			AND message.direction = 1
			AND message.charge > 0
			AND message.service_id IN (
				SELECT id
				FROM service
				WHERE service.parent_object_type_id = object_type_id ('chat')
					AND service.parent_object_id = chat.id
			)
			AND message.status IN (2, 3, 6, 7, 11, 13)
			AND message.delivery_type_id = delivery_type_id ('chat_bill_strict')
	), 0) AS message_failed,

	COALESCE ((
		SELECT sum (message.charge)
		FROM message
		WHERE message.number_id = chat_user.number_id
			AND message.direction = 1
			AND message.charge > 0
			AND message.service_id IN (
				SELECT id
				FROM service
				WHERE service.parent_object_type_id = object_type_id ('chat')
					AND service.parent_object_id = chat.id
			)
			AND message.status IN (1, 8, 9, 12)
			AND message.delivery_type_id = delivery_type_id ('chat_bill_strict')
	), 0) AS message_invalid

FROM chat_user
INNER JOIN chat
	ON chat_user.chat_id = chat.id
WHERE chat_user.type = 'u'
	AND chat_user.number_id IS NOT NULL;
