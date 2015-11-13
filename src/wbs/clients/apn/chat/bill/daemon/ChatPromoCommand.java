package wbs.clients.apn.chat.bill.daemon;

import static wbs.framework.utils.etc.Misc.earlierThan;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.laterThan;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.bill.model.ChatPromoRec;
import wbs.clients.apn.chat.bill.model.ChatPromoUserObjectHelper;
import wbs.clients.apn.chat.bill.model.ChatPromoUserRec;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatPromoCommand")
public
class ChatPromoCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatPromoUserObjectHelper chatPromoUserHelper;

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
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// state

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat_promo.promo"
		};

	}

	@Override
	public
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		ChatPromoRec chatPromo =
			(ChatPromoRec) (Object)
			objectManager.getParent (
				command);

		ChatRec chat =
			chatPromo.getChat ();

		MessageRec message =
			inbox.getMessage ();

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		CommandRec magicCommand =
			commandHelper.findByCode (
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCode (
				chat,
				"help");

		ServiceRec promoService =
			serviceHelper.findByCode (
				chat,
				"promo");

		// send barred users to help

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				chatUser,
				true,
				Optional.of (
					message.getThreadId ()));

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				message.getText ().getText (),
				command,
				true);

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (
					promoService),
				Optional.of (
					affiliate),
				command);

		}

		// log inbound message

		ChatHelpLogRec inboundHelpLog =
			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				message.getText ().getText (),
				command,
				false);

		// check promo has started

		if (
			earlierThan (
				transaction.now (),
				chatPromo.getStartTime ())
		) {

			MessageRec outboundMessage =
				chatSendLogic.sendMessageMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					chatPromo.getPromoNotStartedText (),
					magicCommand,
					promoService,
					helpCommand.getId ());

			chatHelpLogLogic.createChatHelpLogOut (
				chatUser,
				Optional.of (
					inboundHelpLog),
				Optional.<UserRec>absent (),
				outboundMessage,
				Optional.<ChatMessageRec>absent (),
				outboundMessage.getText ().getText (),
				Optional.of (
					helpCommand));

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (
					promoService),
				Optional.of (
					affiliate),
				command);

		}

		// check promo has not ended

		if (
			laterThan (
				transaction.now (),
				chatPromo.getEndTime ())
		) {

			MessageRec outboundMessage =
				chatSendLogic.sendMessageMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					chatPromo.getPromoEndedText (),
					magicCommand,
					promoService,
					helpCommand.getId ());

			chatHelpLogLogic.createChatHelpLogOut (
				chatUser,
				Optional.of (
					inboundHelpLog),
				Optional.<UserRec>absent (),
				outboundMessage,
				Optional.<ChatMessageRec>absent (),
				outboundMessage.getText ().getText (),
				Optional.of (
					helpCommand));

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (
					promoService),
				Optional.of (
					affiliate),
				command);

		}

		// check promo has not been claimed already

		ChatPromoUserRec existingChatPromoUser =
			chatPromo.getChatPromoUsers ().get (
				chatUser.getId ());

		if (
			isNotNull (
				existingChatPromoUser)
		) {

			MessageRec outboundMessage =
				chatSendLogic.sendMessageMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					chatPromo.getAlreadyClaimedText (),
					magicCommand,
					promoService,
					helpCommand.getId ());

			chatHelpLogLogic.createChatHelpLogOut (
				chatUser,
				Optional.of (
					inboundHelpLog),
				Optional.<UserRec>absent (),
				outboundMessage,
				Optional.<ChatMessageRec>absent (),
				outboundMessage.getText ().getText (),
				Optional.of (
					helpCommand));

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (
					promoService),
				Optional.of (
					affiliate),
				command);

		}

		// claim promo

		chatPromoUserHelper.insert (
			chatPromoUserHelper.createInstance ()

			.setChatPromo (
				chatPromo)

			.setChatUser (
				chatUser)

			.setTimestamp (
				transaction.now ())

			.setMessage (
				message)

		);

		chatUser

			.setCredit (
				+ chatUser.getCredit ()
				+ chatPromo.getAmount ())

			.setCreditAdded (
				+ chatUser.getCreditAdded ()
				+ chatPromo.getAmount ());

		MessageRec outboundMessage =
			chatSendLogic.sendMessageMagic (
				chatUser,
				Optional.of (
					message.getThreadId ()),
				chatPromo.getSuccessText (),
				magicCommand,
				promoService,
				helpCommand.getId ());

		chatHelpLogLogic.createChatHelpLogOut (
			chatUser,
			Optional.of (
				inboundHelpLog),
			Optional.<UserRec>absent (),
			outboundMessage,
			Optional.<ChatMessageRec>absent (),
			outboundMessage.getText ().getText (),
			Optional.of (
				helpCommand));

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				promoService),
			Optional.of (
				affiliate),
			command);

	}

}
