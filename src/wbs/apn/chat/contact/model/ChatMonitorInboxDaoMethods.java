package wbs.apn.chat.contact.model;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatMonitorInboxDaoMethods {

	ChatMonitorInboxRec find (
			Transaction parentTransaction,
			ChatUserRec monitorChatUser,
			ChatUserRec userChatUser);

}