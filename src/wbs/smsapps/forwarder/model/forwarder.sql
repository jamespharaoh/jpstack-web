
CREATE INDEX forwarder_message_in_sendqueue
ON forwarder_message_in (retry_timestamp)
WHERE sendqueue;

CREATE UNIQUE INDEX forwarder_message_out_other_id
ON forwarder_message_out (forwarder_id, other_id);

CREATE INDEX forwarder_message_out_pending1
ON forwarder_message_out (forwarder_id, report_retry_time)
WHERE report_index_pending IS NOT NULL;

CREATE INDEX forwarder_message_out_pending2
ON forwarder_message_out (forwarder_id, id)
WHERE report_index_pending IS NOT NULL;

CREATE INDEX forwarder_message_out_report_index
ON forwarder_message_out_report (forwarder_message_out_id, index);
