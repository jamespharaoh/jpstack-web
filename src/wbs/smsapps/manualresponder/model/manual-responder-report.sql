---------------------------------------- VIEW manual_responder_report

CREATE OR REPLACE VIEW manual_responder_report
AS SELECT

	manual_responder_request.id AS manual_responder_request_id,
	manual_responder_number.manual_responder_id AS manual_responder_id,
	manual_responder_request.user_id AS user_id,
	manual_responder_request.user_id AS processed_by_user_id,
	manual_responder_request.queue_item_id as queue_item_id,
	manual_responder_request.timestamp AS request_time,
	manual_responder_request.processed_time AS processed_time,

	to_char (
		manual_responder_request.timestamp,
		 'YYYY-MM-DD HH24:MI:SS'
	) AS timestring,

	manual_responder_request.num_billed_messages :: bigint as num

FROM manual_responder_request
INNER JOIN manual_responder_number
	ON manual_responder_request.manual_responder_number_id
		= manual_responder_number.id;
