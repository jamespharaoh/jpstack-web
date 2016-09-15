package wbs.apn.chat.user.core.model;

import java.util.List;

import wbs.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatUserNoteDaoMethods {

	List<ChatUserNoteRec> find (
			ChatUserRec chatUser);

}