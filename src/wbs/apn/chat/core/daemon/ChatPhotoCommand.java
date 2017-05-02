package wbs.apn.chat.core.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.IdObject;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

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

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;

@Accessors (fluent = true)
@PrototypeComponent ("chatPhotoCommand")
public
class ChatPhotoCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatInfoLogic chatInfoLogic;

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

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
			"chat.get_photo"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"handle");

		) {

			ChatRec chat =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						command));

			ServiceRec defaultService =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"default");

			MessageRec message =
				inbox.getMessage ();

			ChatUserRec chatUser =
				chatUserHelper.findOrCreate (
					transaction,
					chat,
					message);

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					chatUser);

			// send barred users to help

			ChatCreditCheckResult creditCheckResult =
				chatCreditLogic.userSpendCreditCheck (
					transaction,
					chatUser,
					true,
					optionalOf (
						message.getThreadId ()));

			if (creditCheckResult.failed ()) {

				chatHelpLogLogic.createChatHelpLogIn (
					transaction,
					chatUser,
					message,
					rest,
					optionalAbsent (),
					true);

			} else {

				String text =
					rest.trim ();

				Optional <ChatUserRec> photoUserOptional =
					chatUserHelper.findByCode (
						transaction,
						chat,
						text);

				if (text.length () == 0) {

					// just send any three users

					chatInfoLogic.sendUserPics (
						transaction,
						chatUser,
						3l,
						optionalOf (
							message.getThreadId ()));

				} else if (

					optionalIsNotPresent (
						photoUserOptional)

					|| ! chatUserLogic.valid (
						transaction,
						photoUserOptional.get ())

				) {

					// send no such user error

					chatSendLogic.sendSystemMagic (
						transaction,
						chatUser,
						optionalOf (
							message.getThreadId ()),
						"request_photo_error",
						commandHelper.findByCodeRequired (
							transaction,
							chat,
							"magic"),
						IdObject.objectId (
							commandHelper.findByCodeRequired (
								transaction,
								chat,
								"help")),
						TemplateMissing.error,
						emptyMap ());

				} else if (
					isNull (
						photoUserOptional.get ().getMainChatUserImage ())
				) {

					// send no such photo error

					chatSendLogic.sendSystemMagic (
						transaction,
						chatUser,
						optionalOf (
							message.getThreadId ()),
						"no_photo_error",
						commandHelper.findByCodeRequired (
							transaction,
							chat,
							"magic"),
						IdObject.objectId (
							commandHelper.findByCodeRequired (
								transaction,
								chat,
								"help")),
						TemplateMissing.error,
						emptyMap ());

				} else {

					// send pics

					chatInfoLogic.sendRequestedUserPicandOtherUserPics (
						transaction,
						chatUser,
						photoUserOptional.get (),
						2l,
						optionalOf (
							message.getThreadId ()));

				}

			}

			// process inbox

			return smsInboxLogic.inboxProcessed (
				transaction,
				inbox,
				optionalOf (
					defaultService),
				optionalOf (
					affiliate),
				command);

		}

	}

}
