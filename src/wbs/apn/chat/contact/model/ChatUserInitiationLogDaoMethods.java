package wbs.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.core.model.ChatRec;

public
interface ChatUserInitiationLogDaoMethods {

	List <ChatUserInitiationLogRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval);

	List <Long> searchIds (
			ChatUserInitiationLogSearch search);

}