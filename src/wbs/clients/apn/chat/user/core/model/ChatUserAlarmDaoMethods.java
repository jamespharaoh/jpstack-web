package wbs.clients.apn.chat.user.core.model;

import java.util.List;

public
interface ChatUserAlarmDaoMethods {

	List<ChatUserAlarmRec> findPending ();

	ChatUserAlarmRec find (
			ChatUserRec chatUser,
			ChatUserRec monitorChatUser);

}