package wbs.clients.apn.chat.help.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateObjectHelper;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.clients.apn.chat.scheme.model.ChatSchemeRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("chatTemplateLogic")
public
class ChatHelpTemplateLogicImpl
	implements ChatHelpTemplateLogic {

	@Inject
	ChatHelpTemplateObjectHelper chatHelpTemplateHelper;

	@Override
	public
	ChatHelpTemplateRec findChatHelpTemplate (
			ChatUserRec chatUser,
			String type,
			String code) {

		ChatRec chat =
			chatUser.getChat ();

		ChatSchemeRec chatScheme =
			chatUser.getChatScheme ();

		// try and find a scheme specific template

		if (chatScheme != null) {

			ChatHelpTemplateRec chatHelpTemplate =
				chatHelpTemplateHelper.findByTypeAndCode (
					chat,
					type,
					stringFormat (
						"%s_%s",
						code,
						chatScheme.getCode ()));

			if (chatHelpTemplate != null)
				return chatHelpTemplate;

		}

		// try and find a general template

		return chatHelpTemplateHelper.findByTypeAndCode (
			chat,
			type,
			code);

	}

}
