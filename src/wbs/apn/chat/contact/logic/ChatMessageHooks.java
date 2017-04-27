package wbs.apn.chat.contact.logic;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHooks;

import wbs.apn.chat.contact.model.ChatContactObjectHelper;
import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatMessageHooks
	implements ObjectHooks <ChatMessageRec> {

	// dependencies

	@WeakSingletonDependency
	ChatContactObjectHelper chatContactHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatMessageRec chatMessage) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"beforeInsert");

		) {

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
						integerToDecimalString (
							fromChatUser.getChat ().getId ()),
						integerToDecimalString (
							toChatUser.getChat ().getId ())));

			}

			// find or create chat contact

			ChatContactRec chatContact =
				chatContactHelper.findOrCreate (
					taskLogger,
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

}