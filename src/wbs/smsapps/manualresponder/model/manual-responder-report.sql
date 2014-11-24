---------------------------------------- VIEW manual_responder_report

CREATE OR REPLACE VIEW manual_responder_report
AS SELECT

	req.id AS manual_responder_request_id,
	mr.id AS manual_responder_id,
	u.id AS user_id,
	to_char (req.timestamp, 'YYYY-MM-DD HH:MM:SS') AS timestring,

	(
		SELECT count (*)
		FROM message
		WHERE thread_message_id = req.message_id
			AND direction = 1
			AND charge > 0
	) AS num

FROM manual_responder_request AS req

INNER JOIN manual_responder AS mr
	ON req.manual_responder_id = mr.id

INNER JOIN queue_item AS qi
	ON req.queue_item_id = qi.id

INNER JOIN "user" AS u
	ON qi.processed_user_id = u.id;