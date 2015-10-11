package wbs.clients.apn.chat.contact.model;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import wbs.clients.apn.chat.contact.model.ChatContactRec.ChatContactCache;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.AbstractObjectHooks;

public
class ChatContactHooks
	extends AbstractObjectHooks<ChatContactRec> {

	// dependencies

	@Inject
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

		Pair<Integer,Integer> cacheKey =
			Pair.of (
				chatContact.getFromUser ().getId (),
				chatContact.getToUser ().getId ());

		chatContactCache.byUserIds.put (
			cacheKey,
			chatContact);

	}

}