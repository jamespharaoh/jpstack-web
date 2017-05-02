package wbs.apn.chat.user.core.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface ChatUserAlarmDaoMethods {

	List <ChatUserAlarmRec> findPending (
			Transaction parentTransaction,
			Instant now);

	ChatUserAlarmRec find (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			ChatUserRec monitorChatUser);

}