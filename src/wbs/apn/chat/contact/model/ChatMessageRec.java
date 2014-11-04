package wbs.apn.chat.contact.model;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.database.Database;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.media.model.MediaRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class ChatMessageRec
	implements CommonRecord<ChatMessageRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	ChatRec chat;

	@ReferenceField (
		nullable = true)
	ChatContactRec chatContact;

	@SimpleField (
		nullable = true)
	Integer index;

	@ReferenceField
	ChatUserRec fromUser;

	@ReferenceField
	ChatUserRec toUser;

	@SimpleField
	Date timestamp = new Date ();

	@SimpleField (
		nullable = true)
	Date moderatorTimestamp;

	@SimpleField (
		nullable = true)
	Integer threadId;

	@SimpleField (
		nullable = true)
	ChatMessageMethod source;

	@SimpleField (nullable = true)
	ChatMessageMethod method = ChatMessageMethod.sms;

	@ReferenceField
	TextRec originalText;

	@ReferenceField (
		nullable = true)
	TextRec editedText;

	@SimpleField
	Boolean monitorWarning = false;

	@ReferenceField (
		column = "sender_user_id",
		nullable = true)
	UserRec sender;

	@ReferenceField (
		column = "moderator_user_id",
		nullable = true)
	UserRec moderator;

	@SimpleField
	ChatMessageStatus status;

	@ReferenceField (
		nullable = true)
	QueueItemRec queueItem;

	@SimpleField (
		nullable = true)
	Integer deliveryId;

	@LinkField (
		table = "chat_message_media",
		index = "index")
	List<MediaRec> medias =
		new ArrayList<MediaRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<ChatMessageRec> otherRecord) {

		ChatMessageRec other =
			(ChatMessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getChat (),
				other.getChat ())

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// object hooks

	public static
	class ChatMessageHooks
		extends AbstractObjectHooks<ChatMessageRec> {

		@Inject
		Provider<ChatContactObjectHelper> chatContactHelper;

		@Inject
		Database database;

		@Override
		public
		void beforeInsert (
				ChatMessageRec chatMessage) {

			ChatUserRec fromChatUser =
				chatMessage.getFromUser ();

			ChatUserRec toChatUser =
				chatMessage.getToUser ();

			ChatRec chat =
				fromChatUser.getChat ();

			// sanity check

			if (! equal (
					fromChatUser.getChat (),
					toChatUser.getChat ())) {

				throw new RuntimeException (
					stringFormat (
						"From user's chat %s does not match to user's %s",
						fromChatUser.getChat (),
						toChatUser.getChat ()));

			}

			// find or create chat contact

			ChatContactRec chatContact =
				chatContactHelper.get ().findOrCreate (
					chatMessage.getFromUser (),
					chatMessage.getToUser ());

			// update message

			chatMessage
				.setIndex (chatContact.getNumChatMessages ())
				.setChatContact (chatContact)
				.setChat (chat);

			// update chat contact

			chatContact

				.setNumChatMessages (
					chatContact.getNumChatMessages () + 1)

				.setFirstMessageTime (
					ifNull (
						chatContact.getFirstMessageTime (),
						chatMessage.getTimestamp ()))

				.setLastMessageTime (
					chatMessage.getTimestamp ());

		}

	}

	public static
	interface ChatMessageDaoMethods {

		// messages pending signup

		ChatMessageRec findSignup (
				ChatUserRec chatUser);

		List<ChatMessageRec> findSignupTimeout (
				ChatRec chat,
				Instant timestamp);

		// messages sent by console user

		List<ChatMessageRec> findBySenderAndTimestamp (
				ChatRec chat,
				UserRec senderUser,
				Interval timestampInterval);

		// all messages to/from user

		int count (
				ChatUserRec chatUser);

		List<ChatMessageRec> find (
				ChatUserRec chatUser);

		List<ChatMessageRec> findLimit (
				ChatUserRec chatUser,
				int maxResults);

		// all messages between two users

		List<ChatMessageRec> find (
				ChatUserRec fromChatUser,
				ChatUserRec toChatUser);

		List<ChatMessageRec> findLimit (
				ChatUserRec fromChatUser,
				ChatUserRec toChatUser,
				int maxResults);

		// search

		List<ChatMessageRec> search (
				ChatMessageSearch search);

	}

}
