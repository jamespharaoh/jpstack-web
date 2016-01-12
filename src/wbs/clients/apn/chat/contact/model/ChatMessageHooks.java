package wbs.clients.apn.chat.contact.model;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.database.Database;
import wbs.framework.object.AbstractObjectHooks;

public
class ChatMessageHooks
	extends AbstractObjectHooks<ChatMessageRec> {

	// dependencies

	@Inject
	Database database;

	// indirect dependencies

	@Inject
	Provider<ChatContactObjectHelper> chatContactHelper;

	// implementation

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

		if (
			notEqual (
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
				(int) (long)
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