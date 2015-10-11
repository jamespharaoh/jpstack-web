package wbs.clients.apn.chat.broadcast.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.clients.apn.chat.core.model.ChatRec;

public
interface ChatBroadcastDaoMethods {

	List<ChatBroadcastRec> findRecentWindow (
			ChatRec chat,
			int firstResult,
			int maxResults);

	List<ChatBroadcastRec> findSending ();

	List<ChatBroadcastRec> findScheduled (
			Instant now);

}