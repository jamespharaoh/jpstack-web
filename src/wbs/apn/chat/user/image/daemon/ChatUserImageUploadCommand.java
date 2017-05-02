package wbs.apn.chat.user.image.daemon;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
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

import wbs.utils.random.RandomLogic;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("chatUserImageUploadCommand")
public
class ChatUserImageUploadCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

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
	RandomLogic randomLogic;

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
			"chat.image_upload"
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

			MessageRec messageIn =
				inbox.getMessage ();

			ChatRec chat =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						command));

			ChatUserRec chatUser =
				chatUserHelper.findOrCreate (
					transaction,
					chat,
					messageIn);

			ServiceRec defaultService =
				serviceHelper.findByCodeRequired (
					transaction,
					chat,
					"default");

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					transaction,
					chatUser);

			// generate token and url

			String token =
				randomLogic.generateLowercase (10);

			String url =
				stringFormat (
					"http://txtit.com/iu/%u",
					token);

			// send message

			CommandRec magicCommand =
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"magic");

			CommandRec helpCommand =
				commandHelper.findByCodeRequired (
					transaction,
					chat,
					"help");

			MessageRec messageOut =
				optionalGetRequired (
					chatSendLogic.sendSystemMagic (
						transaction,
						chatUser,
						optionalOf (
							messageIn.getThreadId ()),
						"image_upload_link",
						magicCommand,
						helpCommand.getId (),
						TemplateMissing.error,
						ImmutableMap.of (
							"url",
							url)));

			// save token in database

			chatUserImageUploadTokenHelper.insert (
				transaction,
				chatUserImageUploadTokenHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setIndex (
					chatUser.getNumImageUploadTokens ())

				.setToken (
					token)

				.setMessageIn (
					messageIn)

				.setMessageOut (
					messageOut)

				.setCreatedTime (
					transaction.now ())

				.setExpiryTime (
					transaction.now ().plus (
						Duration.standardHours (24)))

			);

			// update chat user

			chatUser

				.setNumImageUploadTokens (
					chatUser.getNumImageUploadTokens () + 1);

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
