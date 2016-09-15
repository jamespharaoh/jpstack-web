package wbs.apn.chat.help.logic;

import java.util.Map;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.sms.gsm.MessageSplitter;

public
interface ChatHelpTemplateLogic {

	ChatHelpTemplateRec findChatHelpTemplate (
			ChatUserRec chatUser,
			String type,
			String code);

	MessageSplitter.Templates splitter (
			ChatRec chat,
			String templateCode,
			Map<String,String> substitutions);

}
