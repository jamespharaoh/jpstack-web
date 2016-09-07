package wbs.clients.apn.chat.adult.daemon;

import java.util.Collections;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.clients.apn.chat.bill.model.ChatUserCreditObjectHelper;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeChargesObjectHelper;
import wbs.clients.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateRec;
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
@PrototypeComponent ("chatAdultVerifyCommand")
public
class ChatAdultVerifyCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ChatSchemeChargesObjectHelper chatSchemeChargesHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserCreditObjectHelper chatUserCreditHelper;

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

		return new String[] {
			"chat.adult_verify"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		ChatRec chat =
			chatHelper.findRequired (
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

		// ignore if there is no chat scheme

		if (chatUser.getChatScheme () == null) {

			return smsInboxLogic.inboxNotProcessed (
				inbox,
				Optional.of (
					defaultService),
				Optional.fromNullable (
					affiliate),
				Optional.of (
					command),
				"No chat scheme for chat user");

		}

		// mark the user as adult verified

		boolean alreadyVerified =
			chatUser.getAdultVerified ();

		chatUserLogic.adultVerify (chatUser);

		// credit the user

		ChatSchemeChargesRec chatSchemeCharges =
			chatSchemeChargesHelper.findRequired (
				chatUser.getChatScheme ().getId ());

		long credit =
			chatSchemeCharges.getAdultVerifyCredit ();

		if (! alreadyVerified && credit > 0) {

			chatUserCreditHelper.insert (
				chatUserCreditHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setCreditAmount (
					credit)

				.setBillAmount (
					0l)

				.setGift (
					true)

				.setDetails (
					"adult verify")

			);

			chatUser

				.setCredit (
					chatUser.getCredit () + credit)

				.setCreditBought (
					+ chatUser.getCreditBought ()
					+ credit);

		}

		// send them a message to confirm

		chatSendLogic.sendSystemRbFree (
			chatUser,
			Optional.of (message.getThreadId ()),
			alreadyVerified
				? "adult_already"
				: "adult_confirm",
			TemplateMissing.error,
			Collections.<String,String>emptyMap ());

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

}