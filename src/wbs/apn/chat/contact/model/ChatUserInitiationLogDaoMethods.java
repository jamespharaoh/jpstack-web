package wbs.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.database.Transaction;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatUserInitiationLogDaoMethods {

	List <ChatUserInitiationLogRec> findByTimestamp (
			Transaction parentTransaction,
			ChatRec chat,
			Interval timestampInterval);

	List <Long> searchIds (
			Transaction parentTransaction,
			ChatUserInitiationLogSearch search);

}