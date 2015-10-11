package wbs.clients.apn.chat.user.core.model;

import java.util.List;

public
interface ChatUserNoteDaoMethods {

	List<ChatUserNoteRec> find (
			ChatUserRec chatUser);

}