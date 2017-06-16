
CREATE TABLE manual_responder_reply_message (

	manual_responder_reply_id int
	NOT NULL
	REFERENCES manual_responder_reply,

	index int
	NOT NULL,

	PRIMARY KEY (
		manual_responder_reply_id,
		index
	),

	message_id int
	NOT NULL
	REFERENCES message

);

CREATE UNIQUE INDEX manual_responder_number_code
ON manual_responder_number (manual_responder_id, code);

---------- VIEW manual_responder_reply_view

CREATE OR REPLACE VIEW manual_responder_reply_view
AS SELECT

	manual_responder_reply.id AS id,
	manual_responder.id AS manual_responder_id,
	"user".id AS user_id,
	manual_responder_reply.timestamp AS timestamp,

	length (text.text) as num_characters

FROM manual_responder_reply

INNER JOIN manual_responder_request
	ON manual_responder_reply.manual_responder_request_id
		= manual_responder_request.id

INNER JOIN manual_responder_number
	ON manual_responder_request.manual_responder_number_id
		= manual_responder_number.id

INNER JOIN manual_responder
	ON manual_responder_number.manual_responder_id
		= manual_responder.id

INNER JOIN "user"
	ON manual_responder_reply.user_id
		= "user".id

INNER JOIN text
	ON manual_responder_reply.text_id
		= text.id

;
