package wbs.integrations.fonix.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface FonixInboundLogDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			FonixInboundLogSearch fonixInboundLogSearch);

}
