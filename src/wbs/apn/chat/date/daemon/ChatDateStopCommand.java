package wbs.apn.chat.date.daemon;

import static wbs.framework.utils.etc.Misc.equal;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

@PrototypeComponent ("chatDateStopCommand")
public
class ChatDateStopCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatDateLogic chatDateLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatSchemeObjectHelper chatSchemeHelper;

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
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat_scheme.date_stop"
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

		CommandTypeRec commandType =
			command.getCommandType ();

		if (! equal (
				commandType.getCode (),
				"date_stop"))
			throw new RuntimeException ();

		if (! equal (
				commandType.getParentObjectType ().getCode (),
				"chat_scheme"))
			throw new RuntimeException ();

		ChatSchemeRec chatScheme =
			chatSchemeHelper.find (
				command.getParentObjectId ());

		ChatRec chat =
			chatScheme.getChat ();

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		// update dating mode

		chatDateLogic.userDateStuff (
			chatUser,
			null,
			message,
			ChatUserDateMode.none,
			true);

		// log help message

		chatHelpLogLogic.createChatHelpLogIn (
			chatUser,
			message,
			receivedMessage.getRest (),
			command,
			false);

		// process inbox

		inboxLogic.inboxProcessed (
			message,
			serviceHelper.findByCode (
				chat,
				"default"),
			chatUserLogic.getAffiliate (
				chatUser),
			command);

		transaction.commit ();

	}

}
