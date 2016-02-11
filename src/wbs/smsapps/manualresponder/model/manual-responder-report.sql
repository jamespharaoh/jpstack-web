---------------------------------------- VIEW manual_responder_report

CREATE OR REPLACE VIEW manual_responder_report
AS SELECT

	req.id AS manual_responder_request_id,
	req.manual_responder_id AS manual_responder_id,
	req.user_id AS user_id,
	req.user_id AS processed_by_user_id,
	req.queue_item_id as queue_item_id,
	req.timestamp AS request_time,
	req.processed_time AS processed_time,

	to_char (req.timestamp, 'YYYY-MM-DD HH24:MI:SS') AS timestring,

	req.num_billed_messages :: bigint as num

FROM manual_responder_request AS req;
