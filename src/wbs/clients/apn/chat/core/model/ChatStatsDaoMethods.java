package wbs.clients.apn.chat.core.model;

import java.util.List;

import org.joda.time.Interval;

public
interface ChatStatsDaoMethods {

	List<ChatStatsRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval);

}