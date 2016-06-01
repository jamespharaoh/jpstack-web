package wbs.clients.apn.chat.core.daemon;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatBlockAllCommand")
public
class ChatBlockAllCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatObjectHelper chatHelper;

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

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.block_all"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ChatRec chat =
			chatHelper.find (
				command.getParentId ());

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				chat,
				"default");

		MessageRec message =
			inbox.getMessage ();

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		// send barred users to help

		/* disabled at sam wilson's request 10 april 2013

		if (! chatLogic.userSpendCheck (
				chatUser,
				true,
				message.getThreadId (),
				false)) {

			chatHelpLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			return null;
		}
		*/

		// call the block all function

		chatMiscLogic.blockAll (
			chatUser,
			message);

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.fromNullable (affiliate),
			command);

	}

}
