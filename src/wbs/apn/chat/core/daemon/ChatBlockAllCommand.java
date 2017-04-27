package wbs.apn.chat.core.daemon;

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

import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;

@Accessors (fluent = true)
@PrototypeComponent ("chatBlockAllCommand")
public
class ChatBlockAllCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatObjectHelper chatHelper;

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

		return new String [] {
			"chat.block_all"
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

			// send barred users to help

			/* disabled at sam wilson's request 10 april 2013

			if (! chatLogic.userSpendCheck (
					chatUser,
					true,
					message.getThreadId (),
					false)) {

				chatHelpLogic.createChatHelpLogIn (
					chatUser,
					message,
					receivedMessage.getRest (),
					null,
					true);

				return null;
			}
			*/

			// call the block all function

			chatMiscLogic.blockAll (
				taskLogger,
				chatUser,
				optionalOf (
					message));

			// process inbox

			return smsInboxLogic.inboxProcessed (
				taskLogger,
				inbox,
				optionalOf (
					defaultService),
				optionalFromNullable (
					affiliate),
				command);

		}

	}

}
