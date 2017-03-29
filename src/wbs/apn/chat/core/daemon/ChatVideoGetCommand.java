package wbs.apn.chat.core.daemon;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.ArrayList;
import java.util.List;

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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.scheme.model.ChatSchemeRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;

@Accessors (fluent = true)
@PrototypeComponent ("chatVideoGetCommand")
public
class ChatVideoGetCommand
	implements CommandHandler {

	// dependencies

	@SingletonDependency
	ChatHelpLogic chatHelpLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatInfoLogic chatInfoLogic;

	@SingletonDependency
	ChatMiscLogic chatLogic;

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
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

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
			"chat.video_get"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handle");

		MessageRec message =
			inbox.getMessage ();

		ChatRec chat =
			genericCastUnchecked (
				objectManager.getParentRequired (
					command));

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				chat,
				"default");

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				taskLogger,
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
				taskLogger,
				chatUser,
				true,
				optionalOf (
					message.getThreadId ()));

		if (creditCheckResult.failed ()) {

			chatHelpLogLogic.createChatHelpLogIn (
				taskLogger,
				chatUser,
				message,
				rest,
				null,
				true);

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					defaultService),
				optionalOf (
					affiliate),
				command);

		}

		// log request

		chatHelpLogLogic.createChatHelpLogIn (
			taskLogger,
			chatUser,
			message,
			rest,
			command,
			false);

		String text =
			rest.trim ();

		if (text.length () == 0) {

			// just send three random videos

			long numSent =
				chatInfoLogic.sendUserVideos (
					taskLogger,
					chatUser,
					3l,
					optionalOf (
						message.getThreadId ()));

			// send a message if no videos were found

			if (numSent == 0) {

				chatSendLogic.sendSystemRbFree (
					taskLogger,
					chatUser,
					optionalOf (
						message.getThreadId ()),
					"no_videos_error",
					TemplateMissing.error,
					emptyMap ());

			}

		} else {

			// find other user and ensure they have video

			Optional<ChatUserRec> otherUserOptional =
				chatUserHelper.findByCode (
					chat,
					text);

			if (

				optionalIsNotPresent (
					otherUserOptional)

				|| otherUserOptional.get ().getChatUserVideoList ().isEmpty ()

			) {

				chatSendLogic.sendSystemRbFree (
					taskLogger,
					chatUser,
					optionalOf (
						message.getThreadId ()),
					"video_not_found",
					TemplateMissing.error,
					emptyMap ());

				return smsInboxLogic.inboxProcessed (
					taskLogger,
					inbox,
					optionalOf (
						defaultService),
					optionalOf (
						affiliate),
					command);

			}

			ChatUserRec otherUser =
				otherUserOptional.get ();

			// send the reply

			List<MediaRec> medias =
				new ArrayList<MediaRec> ();

			medias.add (
				otherUser.getChatUserVideoList ().get (0).getMedia ());

			medias.add (
				chatInfoLogic.chatUserBlurbMedia (
					taskLogger,
					chatUser,
					otherUser));

			messageSender.get ()

				.threadId (
					message.getThreadId ())

				.number (
					chatUser.getNumber ())

				.messageString (
					taskLogger,
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
					taskLogger,
					"User video")

				.medias (
					medias)

				.send (
					taskLogger);

			// charge for one video

			chatCreditLogic.userSpend (
				taskLogger,
				chatUser,
				0,
				0,
				0,
				0,
				1);

		}

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
