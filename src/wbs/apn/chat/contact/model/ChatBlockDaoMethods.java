package wbs.apn.chat.contact.model;

import wbs.apn.chat.contact.model.ChatBlockRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatBlockDaoMethods {

	ChatBlockRec find (
			ChatUserRec chatUser,
			ChatUserRec blockedChatUser);

}