package wbs.smsapps.broadcast.model;

import java.util.List;

import org.joda.time.Instant;

public
interface BroadcastDaoMethods {

	List<BroadcastRec> findSending ();

	List<BroadcastRec> findScheduled (
			Instant now);

}