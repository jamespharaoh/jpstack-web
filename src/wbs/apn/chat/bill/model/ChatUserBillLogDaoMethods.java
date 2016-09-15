package wbs.apn.chat.bill.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatUserBillLogDaoMethods {

	List<ChatUserBillLogRec> findByTimestamp (
			ChatUserRec chatUser,
			Interval timestampInterval);

}