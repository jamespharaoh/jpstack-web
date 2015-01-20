package wbs.apn.chat.core.daemon;

import java.util.Collections;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
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
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

import com.google.common.base.Optional;

@PrototypeComponent ("chatVideoSetCommand")
public
class ChatVideoSetCommand
	implements CommandHandler {

	// dependencies

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

	// details

	@Override
	public String[] getCommandTypes () {

		return new String [] {
			"chat.video_set"
		};

	}

	// implementation

	@Override
	public
	void handle (
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

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		// log request

		chatHelpLogLogic.createChatHelpLogIn (
			chatUser,
			message,
			receivedMessage.getRest (),
			command,
			false);

		if (chatUserLogic.setVideo (
				chatUser,
				message,
				false)) {

			// send a message

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.of (message.getThreadId ()),
				"video_set_pending",
				Collections.<String,String>emptyMap ());

			// auto join

			chatMiscLogic.userAutoJoin (
				chatUser,
				message);

		} else {

			// no video found

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.of (message.getThreadId ()),
				"video_set_error",
				Collections.<String,String>emptyMap ());

		}

		// process inbox

		inboxLogic.inboxProcessed (
			message,
			defaultService,
			affiliate,
			command);

		// and return

		transaction.commit ();

	}

}
