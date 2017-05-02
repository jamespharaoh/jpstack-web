package wbs.apn.chat.bill.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.database.Transaction;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatUserCreditDaoMethods {

	List <ChatUserCreditRec> findByTimestamp (
			Transaction parentTransaction,
			ChatRec chat,
			Interval timestampInterval);

	List <Long> searchIds (
			Transaction parentTransaction,
			ChatUserCreditSearch search);

}