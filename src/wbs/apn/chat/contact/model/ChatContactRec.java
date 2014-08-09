package wbs.apn.chat.contact.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.Pair;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatContactRec
	implements CommonRecord<ChatContactRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ChatRec chat;

	@IdentityReferenceField
	ChatUserRec fromUser;

	@IdentityReferenceField
	ChatUserRec toUser;

	// details

	@ReferenceField (
		nullable = true)
	ChatContactRec inverseChatContact;

	// statistics

	@SimpleField (
		nullable = true)
	Date firstMessageTime;

	@SimpleField (
		nullable = true)
	Date lastMessageTime;

	@SimpleField (
		nullable = true,
		column = "last_message")
	Date lastDeliveredMessageTime;

	@SimpleField (
		nullable = true,
		column = "last_info")
	Date lastInfoTime;

	@SimpleField (
		nullable = true,
		column = "last_pic")
	Date lastPicTime;

	@SimpleField (
		nullable = true,
		column = "last_video")
	Date lastVideoTime;

	@SimpleField (
		nullable = true)
	Integer numChatMessages = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatContactRec> otherRecord) {

		ChatContactRec other =
			(ChatContactRec) otherRecord;

		return new CompareToBuilder ()
			.append (getFromUser (), other.getFromUser ())
			.append (getToUser (), other.getToUser ())
			.toComparison ();

	}

	// dao methods

	public static
	interface ChatContactDaoMethods {

		ChatContactRec find (
				ChatUserRec fromChatUser,
				ChatUserRec toChatUser);

	}

	// object helper methods

	public static
	interface ChatContactObjectHelperMethods {

		ChatContactRec findOrCreate (
				ChatUserRec fromUser,
				ChatUserRec toUser);

	}

	// object helper implementation

	public static
	class ChatContactObjectHelperImplementation
		implements ChatContactObjectHelperMethods {

		@Inject
		Database database;

		@Inject
		Provider<ChatContactObjectHelper> chatContactHelperProvider;

		@Override
		public
		ChatContactRec findOrCreate (
				ChatUserRec fromUser,
				ChatUserRec toUser) {

			Transaction transaction =
				database.currentTransaction ();

			ChatContactObjectHelper chatContactHelper =
				chatContactHelperProvider.get ();

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

			Pair<Integer,Integer> cacheKey =
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
					chatContactHelper.find (
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

			Pair<Integer,Integer> inverseCacheKey =
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
					chatContactHelper.find (
						toUser,
						fromUser);

				chatContactCache.byUserIds.put (
					inverseCacheKey,
					inverseChatContact);

			}

			// create chat contact

			chatContact =
				chatContactHelper.insert (
					new ChatContactRec ()
						.setChat (fromUser.getChat ())
						.setFromUser (fromUser)
						.setToUser (toUser))
						.setInverseChatContact (inverseChatContact);

			chatContactCache.byUserIds.put (
				cacheKey,
				chatContact);

			// update inverse

			if (inverseChatContact != null) {

				inverseChatContact
					.setInverseChatContact (chatContact);

			}

			// and return

			return chatContact;

		}

	}

	// hooks

	public static
	class ChatContactHooks
		extends AbstractObjectHooks<ChatContactRec> {

		@Inject
		Database database;

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

	// cache

	static
	class ChatContactCache {

		Map<Pair<Integer,Integer>,ChatContactRec> byUserIds =
			new HashMap<Pair<Integer,Integer>,ChatContactRec> ();

	}

}
