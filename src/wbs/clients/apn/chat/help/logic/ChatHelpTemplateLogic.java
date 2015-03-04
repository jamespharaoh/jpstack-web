package wbs.clients.apn.chat.help.logic;

import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatHelpTemplateLogic {

	ChatHelpTemplateRec findChatHelpTemplate (
			ChatUserRec chatUser,
			String type,
			String code);

}
