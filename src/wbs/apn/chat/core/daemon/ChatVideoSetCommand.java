package wbs.apn.chat.core.daemon;

import java.util.Collections;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
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

@Accessors (fluent = true)
@PrototypeComponent ("chatVideoSetCommand")
public
class ChatVideoSetCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

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

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

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
			"chat.video_set"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParentOrNull (
				command);

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				chat,
				"default");

		MessageRec message =
			inbox.getMessage ();

		ChatUserRec chatUser =
			chatUserHelper.findOrCreate (
				chat,
				message);

		AffiliateRec affiliate =
			chatUserLogic.getAffiliate (
				chatUser);

		// log request

		chatHelpLogLogic.createChatHelpLogIn (
			chatUser,
			message,
			rest,
			command,
			false);

		if (
			chatUserLogic.setVideo (
				chatUser,
				message,
				false)
		) {

			// send a message

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.of (message.getThreadId ()),
				"video_set_pending",
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

			// auto join

			chatMiscLogic.userAutoJoin (
				chatUser,
				message,
				true);

		} else {

			// no video found

			chatSendLogic.sendSystemRbFree (
				chatUser,
				Optional.of (message.getThreadId ()),
				"video_set_error",
				TemplateMissing.error,
				Collections.<String,String>emptyMap ());

		}

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				defaultService),
			Optional.of (
				affiliate),
			command);

	}

}
