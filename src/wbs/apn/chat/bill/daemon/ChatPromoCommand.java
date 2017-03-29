package wbs.apn.chat.bill.daemon;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.laterThan;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatPromoRec;
import wbs.apn.chat.bill.model.ChatPromoUserObjectHelper;
import wbs.apn.chat.bill.model.ChatPromoUserRec;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.model.ChatMessageMethod;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.join.daemon.ChatJoiner;
import wbs.apn.chat.user.join.daemon.ChatJoiner.JoinType;

@Accessors (fluent = true)
@PrototypeComponent ("chatPromoCommand")
public
class ChatPromoCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatDateLogic chatDateLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatPromoUserObjectHelper chatPromoUserHelper;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ChatJoiner> chatJoinerProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

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
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		Transaction transaction =
			database.currentTransaction ();

		ChatPromoRec chatPromo =
			genericCastUnchecked (
				objectManager.getParentRequired (
					command));

		ChatRec chat =
			chatPromo.getChat ();

		MessageRec inboundMessage =
			inbox.getMessage ();

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				taskLogger,
				chat,
				inboundMessage);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		CommandRec magicCommand =
			commandHelper.findByCodeRequired (
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCodeRequired (
				chat,
				"help");

		ServiceRec promoService =
			serviceHelper.findByCodeRequired (
				chat,
				"promo");

		// send barred users to help

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userSpendCreditCheck (
				taskLogger,
				chatUser,
				true,
				optionalOf (
					inboundMessage.getThreadId ()));

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				taskLogger,
				chatUser,
				inboundMessage,
				inboundMessage.getText ().getText (),
				command,
				true);

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					promoService),
				optionalOf (
					affiliate),
				command);

		}

		// log inbound message

		ChatHelpLogRec inboundHelpLog =
			chatHelpLogLogic.createChatHelpLogIn (
				taskLogger,
				chatUser,
				inboundMessage,
				inboundMessage.getText ().getText (),
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
					taskLogger,
					chatUser,
					optionalOf (
						inboundMessage.getThreadId ()),
					chatPromo.getPromoNotStartedText (),
					magicCommand,
					promoService,
					helpCommand.getId ());

			chatHelpLogLogic.createChatHelpLogOut (
				taskLogger,
				chatUser,
				optionalOf (
					inboundHelpLog),
				optionalAbsent (),
				outboundMessage,
				optionalAbsent (),
				outboundMessage.getText ().getText (),
				optionalOf (
					helpCommand));

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					promoService),
				optionalOf (
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
					taskLogger,
					chatUser,
					optionalOf (
						inboundMessage.getThreadId ()),
					chatPromo.getPromoEndedText (),
					magicCommand,
					promoService,
					helpCommand.getId ());

			chatHelpLogLogic.createChatHelpLogOut (
				taskLogger,
				chatUser,
				optionalOf (
					inboundHelpLog),
				optionalAbsent (),
				outboundMessage,
				optionalAbsent (),
				outboundMessage.getText ().getText (),
				optionalOf (
					helpCommand));

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					promoService),
				optionalOf (
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
					taskLogger,
					chatUser,
					optionalOf (
						inboundMessage.getThreadId ()),
					chatPromo.getAlreadyClaimedText (),
					magicCommand,
					promoService,
					helpCommand.getId ());

			chatHelpLogLogic.createChatHelpLogOut (
				taskLogger,
				chatUser,
				optionalOf (
					inboundHelpLog),
				optionalAbsent (),
				outboundMessage,
				optionalAbsent (),
				outboundMessage.getText ().getText (),
				optionalOf (
					helpCommand));

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					promoService),
				optionalOf (
					affiliate),
				command);

		}

		// claim promo

		chatPromoUserHelper.insert (
			taskLogger,
			chatPromoUserHelper.createInstance ()

			.setChatPromo (
				chatPromo)

			.setChatUser (
				chatUser)

			.setTimestamp (
				transaction.now ())

			.setMessage (
				inboundMessage)

		);

		chatPromo

			.setNumUsers (
				+ chatPromo.getNumUsers ()
				+ 1);

		chatUser

			.setCredit (
				+ chatUser.getCredit ()
				+ chatPromo.getAmount ())

			.setCreditAdded (
				+ chatUser.getCreditAdded ()
				+ chatPromo.getAmount ());

		MessageRec outboundMessage =
			chatSendLogic.sendMessageMagic (
				taskLogger,
				chatUser,
				optionalOf (
					inboundMessage.getThreadId ()),
				chatPromo.getSuccessText (),
				magicCommand,
				promoService,
				helpCommand.getId ());

		chatHelpLogLogic.createChatHelpLogOut (
			taskLogger,
			chatUser,
			optionalOf (
				inboundHelpLog),
			optionalAbsent (),
			outboundMessage,
			optionalAbsent (),
			outboundMessage.getText ().getText (),
			optionalOf (
				helpCommand));

		// new users jump to join process

		if (
			isNull (
				chatUser.getFirstJoin ())
		) {

			return chatJoinerProvider.get ()

				.chatId (
					chat.getId ())

				.joinType (
					(
						chatPromo.getJoinDating ()
						&& ! chatPromo.getJoinChat ()
					)
						? JoinType.dateSimple
						: JoinType.chatSimple)

				.inbox (
					inbox)

				.rest (
					"")

				.handleInbox (
					taskLogger,
					command);

		}

		// simple join for existing customers

		boolean sendMessage = true;

		if (
			chatPromo.getJoinChat ()
			&& ! chatUser.getOnline ()
		) {

			chatMiscLogic.userJoin (
				taskLogger,
				chatUser,
				sendMessage,
				inboundMessage.getThreadId (),
				ChatMessageMethod.sms);

			sendMessage = false;

		}

		if (allOf (

			() -> chatPromo.getJoinDating (),

			() -> enumEqualSafe (
				chatUser.getDateMode (),
				ChatUserDateMode.none)

		)) {

			chatDateLogic.userDateStuff (
				taskLogger,
				chatUser,
				optionalAbsent (),
				optionalOf (
					inboundMessage),
				chatUser.getMainChatUserImage () != null
					? ChatUserDateMode.photo
					: ChatUserDateMode.text,
				sendMessage);

			sendMessage = false;

		}

		// process inbox

		return smsInboxLogic.inboxProcessed (
			taskLogger,
			inbox,
			optionalOf (
				promoService),
			optionalOf (
				affiliate),
			command);

	}

}
