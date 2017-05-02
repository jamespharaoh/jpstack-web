package wbs.apn.chat.bill.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ChatRebillLogDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			ChatRebillLogSearch search);

}
