package wbs.apn.chat.help.logic;

import java.util.Map;

import wbs.framework.database.Transaction;

import wbs.sms.gsm.MessageSplitter;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatHelpTemplateLogic {

	ChatHelpTemplateRec findChatHelpTemplate (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			String type,
			String code);

	MessageSplitter.Templates splitter (
			Transaction parentTransaction,
			ChatRec chat,
			String templateCode,
			Map <String, String> substitutions);

}
