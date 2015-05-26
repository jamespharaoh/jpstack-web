package wbs.clients.apn.chat.help.daemon;

import java.util.Collections;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.logic.CommandLogic;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("chatHelpCommand")
public
class ChatHelpCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CommandLogic commandLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {
		return new String[] {
			"chat.help"
		};
	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				command);

		ServiceRec defaultService =
			serviceHelper.findByCode (
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

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				message.getThreadId ());

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
				null,
				true);

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (defaultService),
				Optional.of (affiliate),
				command);

		}

		if (rest.length () == 0) {

			// send help error

			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (message.getThreadId ()),
				"help_error",
				commandHelper.findByCode (
					chat,
					"magic"),
				commandHelper.findByCode (
					chat,
					"help"
				).getId (),
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

		} else {

			// store message as help request

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
				commandHelper.findByCode (
					chat,
					"help"),
				true);

		}

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

}
