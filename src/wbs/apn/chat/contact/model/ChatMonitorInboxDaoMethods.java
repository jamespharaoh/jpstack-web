package wbs.apn.chat.contact.model;

import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatMonitorInboxDaoMethods {

	ChatMonitorInboxRec find (
			ChatUserRec monitorChatUser,
			ChatUserRec userChatUser);

}