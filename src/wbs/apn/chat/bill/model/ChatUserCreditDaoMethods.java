package wbs.apn.chat.bill.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.core.model.ChatRec;

public
interface ChatUserCreditDaoMethods {

	List <ChatUserCreditRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval);

	List <Long> searchIds (
			ChatUserCreditSearch search);

}