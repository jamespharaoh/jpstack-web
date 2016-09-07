package wbs.clients.apn.chat.contact.logic;

import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.clients.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatContactRec;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectHooks;

public
class ChatMessageHooks
	implements ObjectHooks <ChatMessageRec> {

	// dependencies

	@WeakSingletonDependency
	Provider <ChatContactObjectHelper> chatContactHelper;

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
			chatContactHelper.get ().findOrCreate (
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