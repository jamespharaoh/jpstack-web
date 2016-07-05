package wbs.clients.apn.chat.date.daemon;

import static wbs.framework.utils.etc.Misc.notEqual;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.date.logic.ChatDateLogic;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.scheme.model.ChatSchemeObjectHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
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
			"chat_scheme.date_stop"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		CommandTypeRec commandType =
			command.getCommandType ();

		if (
			notEqual (
				commandType.getCode (),
				"date_stop")
		) {
			throw new RuntimeException ();
		}

		if (
			notEqual (
				commandType.getParentType ().getCode (),
				"chat_scheme")
		) {
			throw new RuntimeException ();
		}

		ChatSchemeRec chatScheme =
			chatSchemeHelper.findOrNull (
				command.getParentId ());

		ChatRec chat =
			chatScheme.getChat ();

		ServiceRec defaultService =
			serviceHelper.findByCodeOrNull (
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
			rest,
			command,
			false);

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

}
