package wbs.smsapps.broadcast.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface BroadcastDaoMethods {

	List <BroadcastRec> findSending (
			Transaction parentTransaction);

	List <BroadcastRec> findScheduled (
			Transaction parentTransaction,
			Instant now);

}