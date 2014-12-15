package wbs.apn.chat.core.daemon;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

import com.google.common.base.Optional;

@PrototypeComponent ("chatNameCommand")
public
class ChatNameCommand
	implements CommandHandler {

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Override
	public String[] getCommandTypes () {

		return new String [] {
			"chat.name"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		String newName =
			receivedMessage
				.getRest ()
				.replaceAll ("\\s*$", "");

		CommandRec command =
			commandHelper.find (
				commandId);

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				command);

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		// limit name

		if (newName.length () > 16) {

			newName =
				newName.substring (0, 16);

		}

		// process inbox

		inboxLogic.inboxProcessed (
			message,
			serviceHelper.findByCode (chat, "default"),
			chatUserLogic.getAffiliate (chatUser),
			commandHelper.find (commandId));

		// make sure the user can send

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				message.getThreadId ());

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			transaction.commit ();

			return null;

		}

		if (newName.length () > 0) {

			// set name

			chatMiscLogic.chatUserSetName (
				chatUser,
				newName,
				message.getThreadId ());

		} else {

			// send reply

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (message.getThreadId ()),
				"name_error",
				commandHelper.findByCode (chat, "magic"),
				commandHelper.findByCode (chat, "name").getId (),
				null);

		}

		transaction.commit ();

		return null;

	}

}
