package wbs.apn.chat.user.image.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.Duration;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenObjectHelper;
import wbs.apn.chat.user.image.model.ChatUserImageUploadTokenRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.utils.etc.Misc;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("chatUserImageUploadCommand")
public
class ChatUserImageUploadCommand
	implements CommandHandler {

	// dependencies

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserImageUploadTokenObjectHelper chatUserImageUploadTokenHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	// details

	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.image_upload"
		};

	}

	// implementation

	public
	Status handle (
			int commandId,
			ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec command =
			commandHelper.find (
				commandId);

		MessageRec messageIn =
			messageHelper.find (
				receivedMessage.getMessageId ());

		ChatRec chat =
			(ChatRec)
			(Object)
			objectManager.getParent (
				command);

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				messageIn);

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		// generate token and url

		String token =
			Misc.generateTenCharacterToken ();

		String url =
			stringFormat (
				"http://txtit.com/iu/%u",
				token);

		// send message

		CommandRec magicCommand =
			commandHelper.findByCode (
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCode (
				chat,
				"help");

		MessageRec messageOut =
			chatSendLogic.sendSystemMagic (
				chatUser,
				Optional.of (
					messageIn.getThreadId ()),
				"image_upload_link",
				magicCommand,
				helpCommand.getId (),
				ImmutableMap.<String,String>builder ()
					.put ("url", url)
					.build ());

		// save token in database

		chatUserImageUploadTokenHelper.insert (
			new ChatUserImageUploadTokenRec ()

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

		inboxLogic.inboxProcessed (
			messageIn,
			defaultService,
			affiliate,
			command);

		// commit transaction and return

		transaction.commit ();

		return null;

	}

}
