package wbs.clients.apn.chat.date.daemon;

import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
import wbs.framework.application.annotations.SingletonDependency;
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
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatDateStopCommand")
public
class ChatDateStopCommand
	implements CommandHandler {

	// dependencies

	@SingletonDependency
	ChatDateLogic chatDateLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatSchemeObjectHelper chatSchemeHelper;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
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
			stringNotEqualSafe (
				commandType.getCode (),
				"date_stop")
		) {
			throw new RuntimeException ();
		}

		if (
			stringNotEqualSafe (
				commandType.getParentType ().getCode (),
				"chat_scheme")
		) {
			throw new RuntimeException ();
		}

		ChatSchemeRec chatScheme =
			chatSchemeHelper.findRequired (
				command.getParentId ());

		ChatRec chat =
			chatScheme.getChat ();

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

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

}
