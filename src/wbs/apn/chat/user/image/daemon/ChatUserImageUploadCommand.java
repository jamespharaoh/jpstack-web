package wbs.apn.chat.user.image.daemon;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	RandomLogic randomLogic;

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
			"chat.image_upload"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		MessageRec messageIn =
			inbox.getMessage ();

		ChatRec chat =
			(ChatRec)
			objectManager.getParentOrNull (
				command);

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				messageIn);

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				chat,
				"default");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
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
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCodeRequired (
				chat,
				"help");

		MessageRec messageOut =
			optionalGetRequired (
				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
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
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);


	}

}
