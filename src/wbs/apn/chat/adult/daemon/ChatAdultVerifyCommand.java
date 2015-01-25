package wbs.apn.chat.adult.daemon;

import java.util.Collections;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.apn.chat.bill.model.ChatUserCreditObjectHelper;
import wbs.apn.chat.bill.model.ChatUserCreditRec;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeChargesObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
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

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("chatAdultVerifyCommand")
public
class ChatAdultVerifyCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ChatSchemeChargesObjectHelper chatSchemeChargesHelper;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserCreditObjectHelper chatUserCreditHelper;

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
	Optional<Integer> commandRef;

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

		ChatRec chat =
			chatHelper.find (
				command.getParentObjectId ());

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

		// ignore if there is no chat scheme

		if (chatUser.getChatScheme () == null) {

			return inboxLogic.inboxNotProcessed (
				message,
				Optional.of (defaultService),
				Optional.of (affiliate),
				Optional.of (command),
				"No chat scheme for chat user");

		}

		// mark the user as adult verified

		boolean alreadyVerified =
			chatUser.getAdultVerified ();

		chatUserLogic.adultVerify (chatUser);

		// credit the user

		ChatSchemeChargesRec chatSchemeCharges =
			chatSchemeChargesHelper.find (
				chatUser.getChatScheme ().getId ());

		int credit =
			chatSchemeCharges.getAdultVerifyCredit ();

		if (! alreadyVerified && credit > 0) {

			chatUserCreditHelper.insert (
				new ChatUserCreditRec ()

				.setChatUser (
					chatUser)

				.setCreditAmount (
					credit)

				.setBillAmount (
					0)

				.setGift (
					true)

				.setDetails (
					"adult verify")

			);

			chatUser

				.setCredit (
					chatUser.getCredit () + credit)

				.incCreditBought (
					credit);

		}

		// send them a message to confirm

		chatSendLogic.sendSystemRbFree (
			chatUser,
			Optional.of (message.getThreadId ()),
			alreadyVerified
				? "adult_already"
				: "adult_confirm",
			Collections.<String,String>emptyMap ());

		// process inbox

		return inboxLogic.inboxProcessed (
			message,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

}