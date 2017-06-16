package wbs.integrations.clockworksms.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface ClockworkSmsInboundLogDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			ClockworkSmsInboundLogSearch clockworkSmsInboundLogSearch);

	List <ClockworkSmsInboundLogRec> findOlderThanLimit (
			Transaction parentTransaction,
			Instant timestamp,
			Long maxResults);

}
