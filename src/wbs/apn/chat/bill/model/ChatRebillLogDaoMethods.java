package wbs.apn.chat.bill.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface ChatRebillLogDaoMethods {

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			ChatRebillLogSearch search);

}
