package wbs.clients.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.clients.apn.chat.core.model.ChatRec;

public
interface ChatUserInitiationLogDaoMethods {

	List<ChatUserInitiationLogRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval);

	List<Integer> searchIds (
			ChatUserInitiationLogSearch search);

}