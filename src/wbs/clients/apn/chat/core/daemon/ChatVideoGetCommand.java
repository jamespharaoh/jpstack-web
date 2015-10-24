package wbs.clients.apn.chat.core.daemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.help.logic.ChatHelpLogic;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
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
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.MessageSender;

@Accessors (fluent = true)
@PrototypeComponent ("chatVideoGetCommand")
public
class ChatVideoGetCommand
	implements CommandHandler {

	// dependencies

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
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Provider<MessageSender> messageSender;

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

		return new String [] {
			"chat.video_get"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		MessageRec message =
			inbox.getMessage ();

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
				command);

		ServiceRec defaultService =
			serviceHelper.findByCode (
				chat,
				"default");

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

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
				rest,
				null,
				true);

			return inboxLogic.inboxProcessed (
				inbox,
				Optional.of (defaultService),
				Optional.of (affiliate),
				command);

		}

		// log request

		chatHelpLogLogic.createChatHelpLogIn (
			chatUser,
			message,
			rest,
			command,
			false);

		String text =
			rest.trim ();

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
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

			}

		} else {

			// find other user and ensure they have video

			ChatUserRec otherUser =
				chatUserHelper.findByCode (
					chat,
					text);

			if (
				otherUser == null
				|| otherUser.getChatUserVideoList ().isEmpty ()
			) {

				chatSendLogic.sendSystemRbFree (
					chatUser,
					Optional.of (message.getThreadId ()),
					"video_not_found",
					TemplateMissing.error,
					Collections.<String,String>emptyMap ());

				return inboxLogic.inboxProcessed (
					inbox,
					Optional.of (defaultService),
					Optional.of (affiliate),
					command);

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

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.of (affiliate),
			command);

	}

}
