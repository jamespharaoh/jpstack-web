package wbs.clients.apn.chat.help.logic;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpLogObjectHelper;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("chatHelpLogLogic")
public
class ChatHelpLogLogicImplementation
	implements ChatHelpLogLogic {

	// dependencies

	@Inject
	ChatHelpLogObjectHelper chatHelpLogHelper;

	@Inject
	Database database;

	@Inject
	QueueLogic queueLogic;

	// implementation

	@Override
	public
	ChatHelpLogRec createChatHelpLogIn (
			ChatUserRec chatUser,
			MessageRec message,
			String text,
			CommandRec command,
			boolean queue) {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// create the request first

		ChatHelpLogRec chatHelpLog =
			chatHelpLogHelper.insert (
				chatHelpLogHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				transaction.now ())

			.setMessage (
				message)

			.setText (
				text)

			.setCommand (
				command)

			.setOurNumber (
				message.getNumTo ())

			.setDirection (
				MessageDirection.in)

		);

		// now create the queue item

		if (queue) {

			QueueItemRec queueItem =
				queueLogic.createQueueItem (
					queueLogic.findQueue (chat, "help"),
					chatUser,
					chatHelpLog,
					chatUser.getCode (),
					text);

			chatHelpLog

				.setQueueItem (
					queueItem);

		}

		return chatHelpLog;
	}

	@Override
	public
	ChatHelpLogRec createChatHelpLogOut (
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<ChatHelpLogRec> replyTo,
			@NonNull Optional<UserRec> user,
			@NonNull MessageRec message,
			@NonNull Optional<ChatMessageRec> chatMessage,
			@NonNull String text,
			@NonNull Optional<CommandRec> command) {

		Transaction transaction =
			database.currentTransaction ();

		return chatHelpLogHelper.insert (
			chatHelpLogHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				transaction.now ())

			.setReplyTo (
				replyTo.orNull ())

			.setUser (
				user.orNull ())

			.setMessage (
				message)

			.setChatMessage (
				chatMessage.orNull ())

			.setText (
				text)

			.setCommand (
				command.orNull ())

			.setOurNumber (
				message.getNumFrom ())

			.setDirection (
				MessageDirection.out)

		);

	}

}
