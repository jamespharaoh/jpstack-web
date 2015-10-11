package wbs.clients.apn.chat.bill.model;

import java.util.List;

public
interface ChatDailyLimitLogDaoMethods {

	List<ChatDailyLimitLogRec> findLimitedToday ();

}