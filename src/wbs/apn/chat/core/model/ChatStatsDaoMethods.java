package wbs.apn.chat.core.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsRec;

public
interface ChatStatsDaoMethods {

	List <ChatStatsRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval);

}