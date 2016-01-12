package wbs.clients.apn.chat.help.logic;

import static wbs.framework.utils.etc.Misc.isNotPresent;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.contact.model.ChatMessageRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("chatHelpLogic")
public
class ChatHelpLogicImplementation
	implements ChatHelpLogic {

	// dependencies

	@Inject
	ChatHelpLogLogic chatHelpLogLogic;

	@Inject
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void sendHelpMessage (
			@NonNull UserRec user,
			@NonNull ChatUserRec chatUser,
			@NonNull String text,
			@NonNull Optional<Long> threadId,
			@NonNull Optional<ChatHelpLogRec> replyTo) {

		ChatRec chat =
			chatUser.getChat ();

		CommandRec magicCommand =
			commandHelper.findByCode (
				chat,
				"magic");

		CommandRec helpCommand =
			commandHelper.findByCode (
				chat,
				"help");

		ServiceRec helpService  =
			serviceHelper.findByCode (
				chat,
				"help");

		// load templates

		ChatHelpTemplateRec singleTemplate =
			chatHelpTemplateHelper.findByTypeAndCode (
				chat,
				"system",
				"help_single");

		ChatHelpTemplateRec multipleTemplate =
			chatHelpTemplateHelper.findByTypeAndCode (
				chat,
				"system",
				"help_multiple");

		// split message

		MessageSplitter.Templates messageSplitterTemplates =
			new MessageSplitter.Templates (
				singleTemplate.getText (),
				multipleTemplate.getText (),
				multipleTemplate.getText (),
				multipleTemplate.getText ());

		List<String> splitTexts =
			MessageSplitter.split (
				text,
				messageSplitterTemplates);

		for (
			String splitText
				: splitTexts
		) {

			// send message

			MessageRec message =
				chatSendLogic.sendMessageMagic (
					chatUser,
					threadId,
					textHelper.findOrCreate (
						splitText),
					magicCommand,
					helpService,
					(long) helpCommand.getId ());

			if (
				isNotPresent (
					threadId)
			) {

				threadId =
					Optional.of (
						(long)
						message.getId ());

			}

			// save reply

			chatHelpLogLogic.createChatHelpLogOut (
				chatUser,
				replyTo,
				Optional.of (
					user),
				message,
				Optional.<ChatMessageRec>absent (),
				splitText,
				Optional.of (
					helpCommand));

		}

	}

}
