package wbs.apn.chat.help.logic;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;

import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpLogObjectHelper;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

@SingletonComponent ("chatHelpLogLogic")
public
class ChatHelpLogLogicImplementation
	implements ChatHelpLogLogic {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogObjectHelper chatHelpLogHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	QueueLogic queueLogic;

	// implementation

	@Override
	public
	ChatHelpLogRec createChatHelpLogIn (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec chatUser,
			@NonNull MessageRec message,
			@NonNull String text,
			@NonNull CommandRec command,
			@NonNull Boolean queue) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatHelpLogIn");

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatUser.getChat ();

		// create the request first

		ChatHelpLogRec chatHelpLog =
			chatHelpLogHelper.insert (
				taskLogger,
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
					taskLogger,
					chat,
					"help",
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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ChatUserRec chatUser,
			@NonNull Optional<ChatHelpLogRec> replyTo,
			@NonNull Optional<UserRec> user,
			@NonNull MessageRec message,
			@NonNull Optional<ChatMessageRec> chatMessage,
			@NonNull String text,
			@NonNull Optional<CommandRec> command) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createChatHelpLogOut");

		Transaction transaction =
			database.currentTransaction ();

		return chatHelpLogHelper.insert (
			taskLogger,
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
