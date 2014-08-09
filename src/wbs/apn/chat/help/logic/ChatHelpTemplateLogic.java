package wbs.apn.chat.help.logic;

import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatHelpTemplateLogic {

	ChatHelpTemplateRec findChatHelpTemplate (
			ChatUserRec chatUser,
			String type,
			String code);

}
