package wbs.integrations.oxygenate.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface OxygenateInboundLogDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			OxygenateInboundLogSearch oxygenateInboundLogSearch);

	List <OxygenateInboundLogRec> findOlderThanLimit (
			Transaction parentTransaction,
			Instant timestamp,
			Long maxItems);

}