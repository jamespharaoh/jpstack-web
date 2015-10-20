package wbs.clients.apn.chat.help.logic;

import static wbs.framework.utils.etc.Misc.instantToDate;

import javax.inject.Inject;

import lombok.NonNull;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("chatHelpLogLogic")
public
class ChatHelpLogLogicImpl
	implements ChatHelpLogLogic {

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueLogic queueLogic;

	@Override
	public
	ChatHelpLogRec createChatHelpLogIn (
			ChatUserRec chatUser,
			MessageRec message,
			String text,
			CommandRec command,
			boolean queue) {

		ChatRec chat =
			chatUser.getChat ();

		// create the request first

		ChatHelpLogRec chatHelpLog =
			objectManager.insert (
				new ChatHelpLogRec ()
					.setChatUser (chatUser)
					.setMessage (message)
					.setText (text)
					.setCommand (command)
					.setOurNumber (message.getNumTo ())
					.setDirection (MessageDirection.in));

		// now create the queue item

		if (queue) {

			QueueItemRec queueItem =
				queueLogic.createQueueItem (
					queueLogic.findQueue (chat, "help"),
					chatUser,
					chatHelpLog,
					chatUser.getCode (),
					text);

			queueItem.setPriority (-10);

			chatHelpLog.setQueueItem (queueItem);

		}

		return chatHelpLog;
	}

	@Override
	public
	ChatHelpLogRec createChatHelpLogOut (
			@NonNull ChatUserRec chatUser,
			ChatHelpLogRec replyTo,
			UserRec user,
			@NonNull MessageRec message,
			ChatMessageRec chatMessage,
			@NonNull String text,
			CommandRec command) {

		Transaction transaction =
			database.currentTransaction ();

		return objectManager.insert (
			new ChatHelpLogRec ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setReplyTo (
				replyTo)

			.setUser (
				user)

			.setMessage (
				message)

			.setChatMessage (
				chatMessage)

			.setText (
				text)

			.setCommand (
				command)

			.setOurNumber (
				message.getNumFrom ())

			.setDirection (
				MessageDirection.out)

		);

	}

}
