package wbs.apn.chat.contact.logic;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectHooks;

public
class ChatMessageHooks
	implements ObjectHooks <ChatMessageRec> {

	// dependencies

	@WeakSingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@SingletonDependency
	Database database;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull ChatMessageRec chatMessage) {

		ChatUserRec fromChatUser =
			chatMessage.getFromUser ();

		ChatUserRec toChatUser =
			chatMessage.getToUser ();

		ChatRec chat =
			fromChatUser.getChat ();

		// sanity check

		if (
			referenceNotEqualWithClass (
				ChatRec.class,
				fromChatUser.getChat (),
				toChatUser.getChat ())
		) {

			throw new RuntimeException (
				stringFormat (
					"From user's chat %s does not match to user's %s",
					fromChatUser.getChat (),
					toChatUser.getChat ()));

		}

		// find or create chat contact

		ChatContactRec chatContact =
			chatContactHelper.findOrCreate (
				chatMessage.getFromUser (),
				chatMessage.getToUser ());

		// update message

		chatMessage

			.setIndex (
				chatContact.getNumChatMessages ())

			.setChatContact (
				chatContact)

			.setChat (
				chat);

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