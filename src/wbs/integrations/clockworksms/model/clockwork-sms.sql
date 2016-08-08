CREATE UNIQUE INDEX clockwork_sms_network_their_id
ON clockwork_sms_network (
	clockwork_sms_config_id,
	their_id
);