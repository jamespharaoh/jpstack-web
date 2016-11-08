package wbs.apn.chat.core.daemon;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import java.util.Collections;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.contact.logic.ChatSendLogic.TemplateMissing;
import wbs.apn.chat.help.logic.ChatHelpLogLogic;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.info.logic.ChatInfoLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.entity.record.IdObject;
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
			"chat.get_photo"
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

		} else {

			String text =
				rest.trim ();

			Optional<ChatUserRec> photoUserOptional =
				chatUserHelper.findByCode (
					chat,
					text);

			if (text.length () == 0) {

				// just send any three users

				chatInfoLogic.sendUserPics (
					chatUser,
					3l,
					Optional.of (
						message.getThreadId ()));

			} else if (

				optionalIsNotPresent (
					photoUserOptional)

				|| ! chatUserLogic.valid (
					photoUserOptional.get ())

			) {

				// send no such user error

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"request_photo_error",
					commandHelper.findByCodeRequired (
						chat,
						"magic"),
					IdObject.objectId (
						commandHelper.findByCodeRequired (
							chat,
							"help")),
					TemplateMissing.error,
					Collections.emptyMap ());

			} else if (
				isNull (
					photoUserOptional.get ().getMainChatUserImage ())
			) {

				// send no such photo error

				chatSendLogic.sendSystemMagic (
					chatUser,
					Optional.of (
						message.getThreadId ()),
					"no_photo_error",
					commandHelper.findByCodeRequired (
						chat,
						"magic"),
					IdObject.objectId (
						commandHelper.findByCodeRequired (
							chat,
							"help")),
					TemplateMissing.error,
					Collections.emptyMap ());

			} else {

				// send pics

				chatInfoLogic.sendRequestedUserPicandOtherUserPics (
					chatUser,
					photoUserOptional.get (),
					2l,
					Optional.of (
						message.getThreadId ()));

			}

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
