package wbs.apn.chat.bill.model;

import java.util.List;

public
interface ChatRebillLogDaoMethods {

	List <Long> searchIds (
			ChatRebillLogSearch search);

}
