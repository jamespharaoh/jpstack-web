package wbs.clients.apn.chat.contact.model;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatMonitorInboxDaoMethods {

	ChatMonitorInboxRec find (
			ChatUserRec monitorChatUser,
			ChatUserRec userChatUser);

}