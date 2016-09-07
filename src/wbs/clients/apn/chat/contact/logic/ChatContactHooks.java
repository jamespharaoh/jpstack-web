package wbs.clients.apn.chat.contact.logic;

import org.apache.commons.lang3.tuple.Pair;

import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectHooks;

public
class ChatContactHooks
	implements ObjectHooks <ChatContactRec> {

	// dependencies

	@SingletonDependency
	Database database;

	// implementation

	@Override
	public
	void afterInsert (
			ChatContactRec chatContact) {

		Transaction transaction =
			database.currentTransaction ();

		// get cache

		ChatContactCache chatContactCache =
			(ChatContactCache)
			transaction.getMeta (
				"chatContactCache");

		if (chatContactCache == null) {

			chatContactCache =
				new ChatContactCache ();

			database.currentTransaction ().setMeta (
				"chatContactCache",
				chatContactCache);

		}

		// update cache

		Pair <Long,Long> cacheKey =
			Pair.of (
				chatContact.getFromUser ().getId (),
				chatContact.getToUser ().getId ());

		chatContactCache.byUserIds.put (
			cacheKey,
			chatContact);

	}

}