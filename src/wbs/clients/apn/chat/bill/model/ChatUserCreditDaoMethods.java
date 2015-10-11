package wbs.clients.apn.chat.bill.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.clients.apn.chat.core.model.ChatRec;

public
interface ChatUserCreditDaoMethods {

	List<ChatUserCreditRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval);

}