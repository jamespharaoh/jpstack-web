package wbs.apn.chat.broadcast.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatBroadcastDaoMethods {

	List <ChatBroadcastRec> findRecentWindow (
			Transaction parentTransaction,
			ChatRec chat,
			Long firstResult,
			Long maxResults);

	List <ChatBroadcastRec> findSending (
			Transaction parentTransaction);

	List <ChatBroadcastRec> findScheduled (
			Transaction parentTransaction,
			Instant now);

}