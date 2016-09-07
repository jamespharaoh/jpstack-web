package wbs.clients.apn.chat.contact.logic;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.clients.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatContactObjectHelperMethods;
import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.application.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

public
class ChatContactObjectHelperMethodsImplementation
	implements ChatContactObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@SingletonDependency
	Database database;

	// implementation

	@Override
	public
	ChatContactRec findOrCreate (
			@NonNull ChatUserRec fromUser,
			@NonNull ChatUserRec toUser) {

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

		// look in cache

		ChatContactRec chatContact;

		Pair <Long, Long> cacheKey =
			Pair.of (
				fromUser.getId (),
				toUser.getId ());

		if (chatContactCache.byUserIds.containsKey (
				cacheKey)) {

			chatContact =
				chatContactCache.byUserIds.get (
					cacheKey);

			if (chatContact != null)
				return chatContact;

		} else {

			// look for existing

			chatContact =
				chatContactHelper.findNoFlush (
					fromUser,
					toUser);

			if (chatContact != null) {

				chatContactCache.byUserIds.put (
					cacheKey,
					chatContact);

				return chatContact;

			}

		}

		// find inverse TODO not atomic!

		Pair <Long, Long> inverseCacheKey =
			Pair.of (
				toUser.getId (),
				fromUser.getId ());

		ChatContactRec inverseChatContact;

		if (chatContactCache.byUserIds.containsKey (
				inverseCacheKey)) {

			inverseChatContact =
				chatContactCache.byUserIds.get (
					inverseCacheKey);

		} else {

			inverseChatContact =
				chatContactHelper.findNoFlush (
					toUser,
					fromUser);

			chatContactCache.byUserIds.put (
				inverseCacheKey,
				inverseChatContact);

		}

		// create chat contact

		chatContact =
			chatContactHelper.insert (
				chatContactHelper.createInstance ()

			.setFromUser (
				fromUser)

			.setToUser (
				toUser)

			.setChat (
				fromUser.getChat ())

			.setInverseChatContact (
				inverseChatContact)

		);

		chatContactCache.byUserIds.put (
			cacheKey,
			chatContact);

		// update inverse

		if (inverseChatContact != null) {

			inverseChatContact

				.setInverseChatContact (
					chatContact);

		}

		// and return

		return chatContact;

	}

}