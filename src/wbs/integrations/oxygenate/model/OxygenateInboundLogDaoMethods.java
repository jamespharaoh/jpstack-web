package wbs.integrations.oxygenate.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface OxygenateInboundLogDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			OxygenateInboundLogSearch oxygenateInboundLogSearch);

}