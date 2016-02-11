
CREATE TABLE auto_responder_request_sent_message (

	auto_responder_request_id int
	NOT NULL
	REFERENCES auto_responder_request,

	index int
	NOT NULL,

	PRIMARY KEY (
		auto_responder_request_id,
		index
	),

	message_id int
	NOT NULL
	REFERENCES message

);
