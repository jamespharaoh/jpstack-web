package wbs.clients.apn.chat.date.daemon;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.clients.apn.chat.core.daemon.ChatPatterns;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.date.logic.ChatDateLogic;
import wbs.clients.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDateMode;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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
@PrototypeComponent ("chatDateCommand")
public
class ChatDateCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	ChatDateLogic chatDateLogic;

	@SingletonDependency
	ChatHelpLogLogic chatHelpLogLogic;

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	CommandObjectHelper commandHelper;

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
			"chat.date_join_photo",
			"chat.date_join_text",
			"chat.date_upgrade"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		ChatRec chat =
			(ChatRec) (Object)
			objectManager.getParent (
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

		// work out date mode

		if (! validCommandCodes.contains (
				command.getCode ()))
			throw new RuntimeException ();

		ChatUserDateMode dateMode =
			dateModeByCommandCode.get (
				command.getCode ());

		// do join

		if (
			chatUser.getDateMode () == ChatUserDateMode.none
			&& dateMode != ChatUserDateMode.none
		) {

			// log request

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
				command,
				false);

			// set date mode

			chatDateLogic.userDateStuff (
				chatUser,
				null,
				message,
				dateMode,
				true);

			// process inbox

			return smsInboxLogic.inboxProcessed (
				inbox,
				Optional.of (defaultService),
				Optional.of (affiliate),
				command);

		}

		// do upgrade

		if (
			chatUser.getDateMode () == ChatUserDateMode.text
			&& dateMode == null
			&& ChatPatterns.yes.matcher (rest).find ()
		) {

			// log request

			chatHelpLogLogic.createChatHelpLogIn (
				chatUser,
				message,
				rest,
				command,
				false);

			// update user

			chatDateLogic.userDateStuff (
				chatUser,
				null,
				message,
				dateMode,
				true);

			// process inbox

			return smsInboxLogic.inboxProcessed (
				inbox,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				command);

		}

		// manually process

		chatHelpLogLogic.createChatHelpLogIn (
			chatUser,
			message,
			rest,
			command,
			true);

		// process inbox

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.<ServiceRec>absent (),
			Optional.<AffiliateRec>absent (),
			command);

	}

	final
	static Map<String,ChatUserDateMode> dateModeByCommandCode =
		ImmutableMap.<String,ChatUserDateMode>builder ()

		.put (
			"date_join_photo",
			ChatUserDateMode.photo)

		.put (
			"date_join_text",
			ChatUserDateMode.text)

		.build ();

	final
	static Set<String> validCommandCodes =
		ImmutableSet.<String>builder ()

		.addAll (
			dateModeByCommandCode.keySet ())

		.add (
			"date_upgrade")

		.build ();

}
