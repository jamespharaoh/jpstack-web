package wbs.smsapps.forwarder.model;

import java.util.List;

import org.joda.time.Instant;

public
interface ForwarderMessageInDaoMethods {

	ForwarderMessageInRec findNext (
			Instant now,
			ForwarderRec forwarder);

	List<ForwarderMessageInRec> findNextLimit (
			Instant now,
			int maxResults);

	List<ForwarderMessageInRec> findPendingLimit (
			ForwarderRec forwarder,
			int maxResults);

}