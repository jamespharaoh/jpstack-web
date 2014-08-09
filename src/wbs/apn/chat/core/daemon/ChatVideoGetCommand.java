package wbs.apn.chat.core.daemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.outbox.logic.MessageSender;

import com.google.common.base.Optional;

@PrototypeComponent ("chatVideoGetCommand")
public
class ChatVideoGetCommand
	implements CommandHandler {

	@Inject
	ChatHelpLogic chatHelpLogic;

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatInfoLogic chatInfoLogic;

	@Inject
	ChatMiscLogic chatLogic;

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
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"chat.video_get"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		CommandRec command =
			commandHelper.find (
				commandId);

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				command);

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// set received message stuff

		chatUserLogic.setAffiliateId (
			receivedMessage,
			chatUser);

		chatLogic.setServiceId (
			receivedMessage,
			chat,
			"default");

		// send barred users to help

		if (! chatCreditLogic.userSpendCheck (
				chatUser,
				true,
				message.getThreadId (),
				false)) {

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				receivedMessage.getRest (),
				null,
				true);

			transaction.commit ();

			return Status.processed;

		}

		// log request

		chatHelpLogLogic.createChatHelpLogIn (
			chatUser,
			message,
			receivedMessage.getRest (),
			command,
			false);

		String text =
			receivedMessage.getRest ().trim ();

		if (text.length () == 0) {

			// just send three random videos

			int numSent =
				chatInfoLogic.sendUserVideos (
					chatUser,
					3,
					message.getThreadId ());

			// send a message if no videos were found

			if (numSent == 0) {

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.of (message.getThreadId ()),
					"no_videos_error",
					Collections.<String,String>emptyMap ());

			}

		} else {

			// find other user and ensure they have video

			ChatUserRec otherUser =
				chatUserHelper.findByCode (
					chat,
					text);

			if (otherUser == null
					|| otherUser.getChatUserVideoList ().isEmpty ()) {

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.of (message.getThreadId ()),
					"video_not_found",
					Collections.<String,String>emptyMap ());

				transaction.commit ();
				return Status.processed;
			}

			// send the reply

			List<MediaRec> medias =
				new ArrayList<MediaRec> ();

			medias.add (
				otherUser.getChatUserVideoList ().get (0).getMedia ());

			medias.add (
				chatInfoLogic.chatUserBlurbMedia (
					chatUser,
					otherUser));

			ServiceRec defaultService =
				serviceHelper.findByCode (
					chat,
					"default");

			AffiliateRec affiliate =
				chatUserLogic.getAffiliate (
					chatUser);

			messageSender.get ()

				.threadId (
					message.getThreadId ())

				.number (
					chatUser.getNumber ())

				.messageString (
					"")

				.numFrom (
					chatScheme.getMmsNumber ())

				.route (
					chatScheme.getMmsRoute ())

				.service (
					defaultService)

				.affiliate (
					affiliate)

				.subjectString (
					"User video")

				.medias (
					medias)

				.send ();

			// charge for one video

			chatCreditLogic.userSpend (
				chatUser,
				0,
				0,
				0,
				0,
				1);

		}

		transaction.commit ();

		return Status.processed;

	}

}
