package wbs.apn.chat.core.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.database.Transaction;

public
interface ChatStatsDaoMethods {

	List <ChatStatsRec> findByTimestamp (
			Transaction parentTransaction,
			ChatRec chat,
			Interval timestampInterval);

}