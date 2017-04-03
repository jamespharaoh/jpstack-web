package wbs.apn.chat.user.core.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatUserAlarmDaoMethods {

	List <ChatUserAlarmRec> findPending (
			Instant now);

	ChatUserAlarmRec find (
			ChatUserRec chatUser,
			ChatUserRec monitorChatUser);

}