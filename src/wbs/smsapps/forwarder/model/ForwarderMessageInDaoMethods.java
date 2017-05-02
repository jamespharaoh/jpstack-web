package wbs.smsapps.forwarder.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface ForwarderMessageInDaoMethods {

	ForwarderMessageInRec findNext (
			Transaction parentTransaction,
			Instant now,
			ForwarderRec forwarder);

	List <ForwarderMessageInRec> findNextLimit (
			Transaction parentTransaction,
			Instant now,
			Long maxResults);

	List <ForwarderMessageInRec> findPendingLimit (
			Transaction parentTransaction,
			ForwarderRec forwarder,
			Long maxResults);

}