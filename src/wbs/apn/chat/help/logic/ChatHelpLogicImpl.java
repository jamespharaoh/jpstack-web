package wbs.apn.chat.help.logic;

import java.util.List;

import javax.inject.Inject;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.gsm.MessageSplitter;
import wbs.sms.message.core.model.MessageRec;

import com.google.common.base.Optional;

@SingletonComponent ("chatHelpLogic")
public
class ChatHelpLogicImpl
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
			UserRec user,
			ChatUserRec chatUser,
			String text,
			Integer threadId,
			ChatHelpLogRec replyTo) {

		ChatRec chat =
			chatUser.getChat ();

		// load templates

		ChatHelpTemplateRec singleTemplate =
			chatHelpTemplateHelper.findByCode (
				chat,
				"system",
				"help_single");

		ChatHelpTemplateRec multipleTemplate =
			chatHelpTemplateHelper.findByCode (
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

		for (String splitText : splitTexts) {

			// send message

			MessageRec message =
				chatSendLogic.sendMessageMagic (
					chatUser,
					Optional.fromNullable (threadId),
					textHelper.findOrCreate (splitText),
					commandHelper.findByCode (chat, "magic"),
					serviceHelper.findByCode (chat, "help"),
					commandHelper.findByCode (chat, "help").getId ());

			if (threadId == null)
				threadId = message.getId ();

			// save reply

			chatHelpLogLogic.createChatHelpLogOut (
				chatUser,
				replyTo,
				user,
				message,
				null,
				splitText,
				commandHelper.findByCode (chat, "help"));

		}

	}

}
