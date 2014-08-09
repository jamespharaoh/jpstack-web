package wbs.apn.chat.core.daemon;

import java.util.Collections;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
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

@PrototypeComponent ("chatPhotoCommand")
public
class ChatPhotoCommand
	implements CommandHandler {

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatInfoLogic chatInfoLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

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
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.get_photo"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

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

		// process inbox

		inboxLogic.inboxProcessed (
			message,
			serviceHelper.findByCode (chat, "default"),
			chatUserLogic.getAffiliate (chatUser),
			commandHelper.find (commandId));

		// send barred users to help

		if (! chatCreditLogic.userSpendCheck (
				chatUser,
				true,
				message.getThreadId (),
				false)) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			transaction.commit ();

			return null;
		}

		String text =
			receivedMessage.getRest ().trim ();

		ChatUserRec photoUser =
			chatUserHelper.findByCode (
				chat,
				text);

		if (text.length () == 0) {

			// just send any three users

			chatInfoLogic.sendUserPics (
				chatUser,
				3,
				message.getThreadId ());

		} else if (! chatUserLogic.valid (photoUser)) {

			// send no such user error

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (message.getThreadId ()),
				"request_photo_error",
				commandHelper.findByCode (chat, "magic"),
				commandHelper.findByCode (chat, "help").getId (),
				Collections.<String,String>emptyMap ());

		} else if (photoUser.getMainChatUserImage () == null) {

			// send no such photo error

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (message.getThreadId ()),
				"no_photo_error",
				commandHelper.findByCode (chat, "magic"),
				commandHelper.findByCode (chat, "help").getId (),
				Collections.<String,String>emptyMap ());

		} else {

			// send pics

			chatInfoLogic.sendRequestedUserPicandOtherUserPics (
				chatUser,
				photoUser,
				2,
				message.getThreadId ());

		}

		transaction.commit ();

		return null;

	}

}
