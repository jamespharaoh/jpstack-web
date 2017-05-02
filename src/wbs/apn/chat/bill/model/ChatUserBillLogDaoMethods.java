package wbs.apn.chat.bill.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatUserBillLogDaoMethods {

	List <ChatUserBillLogRec> findByTimestamp (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Interval timestampInterval);

}