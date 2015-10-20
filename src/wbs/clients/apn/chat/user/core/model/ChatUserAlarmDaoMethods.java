package wbs.clients.apn.chat.user.core.model;

import java.util.List;

import org.joda.time.Instant;

public
interface ChatUserAlarmDaoMethods {

	List<ChatUserAlarmRec> findPending (
			Instant now);

	ChatUserAlarmRec find (
			ChatUserRec chatUser,
			ChatUserRec monitorChatUser);

}