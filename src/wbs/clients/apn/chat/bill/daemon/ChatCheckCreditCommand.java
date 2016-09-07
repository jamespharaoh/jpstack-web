package wbs.clients.apn.chat.bill.daemon;

import static wbs.framework.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

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
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatCheckCreditCommand")
public
class ChatCheckCreditCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional <Long> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.check_credit"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ChatRec chat =
			(ChatRec)
			objectManager.getParent (
				command);

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

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				optionalOf (
					message.getThreadId ()));

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
				null,
				true);

			return smsInboxLogic.inboxProcessed (
				inbox,
				optionalOf (
					defaultService),
				optionalOf (
					affiliate),
				command);

		}

		// otherwise send their credit

		String creditString =
			currencyLogic.formatText (
				chat.getCurrency (),
				chatUser.getCredit ());

		chatSendLogic.sendSystemRbFree (
			chatUser,
			optionalOf (
				message.getThreadId ()),
			"check_credit",
			TemplateMissing.error,
			ImmutableMap.of (
				"credit",
				creditString));

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			optionalOf (
				defaultService),
			optionalOf (
				affiliate),
			command);

	}

}
