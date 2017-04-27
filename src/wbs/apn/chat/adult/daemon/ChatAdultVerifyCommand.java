package wbs.apn.chat.adult.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

import wbs.apn.chat.bill.model.ChatUserCreditObjectHelper;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.scheme.model.ChatSchemeChargesObjectHelper;
import wbs.apn.chat.scheme.model.ChatSchemeChargesRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handle");

		) {

			BorrowedTransaction transaction =
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
					taskLogger,
					chat,
					message);

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatUser);

			// ignore if there is no chat scheme

			if (chatUser.getChatScheme () == null) {

				return smsInboxLogic.inboxNotProcessed (
					taskLogger,
					inbox,
					optionalOf (
						defaultService),
					optionalFromNullable (
						affiliate),
					optionalOf (
						command),
					"No chat scheme for chat user");

			}

			// mark the user as adult verified

			boolean alreadyVerified =
				chatUser.getAdultVerified ();

			chatUserLogic.adultVerify (
				taskLogger,
				chatUser);

			// credit the user

			ChatSchemeChargesRec chatSchemeCharges =
				chatSchemeChargesHelper.findRequired (
					chatUser.getChatScheme ().getId ());

			long credit =
				chatSchemeCharges.getAdultVerifyCredit ();

			if (! alreadyVerified && credit > 0) {

				chatUserCreditHelper.insert (
					taskLogger,
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
				taskLogger,
				chatUser,
				optionalOf (
					message.getThreadId ()),
				alreadyVerified
					? "adult_already"
					: "adult_confirm",
				TemplateMissing.error,
				emptyMap ());

			// process inbox

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					defaultService),
				optionalOf (
					affiliate),
				command);

		}

	}

}