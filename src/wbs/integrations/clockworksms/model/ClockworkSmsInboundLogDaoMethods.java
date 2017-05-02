package wbs.integrations.clockworksms.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ClockworkSmsInboundLogDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			ClockworkSmsInboundLogSearch clockworkSmsInboundLogSearch);

}
