package wbs.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactNoteDaoMethods {

	List<ChatContactNoteRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval);

	List<ChatContactNoteRec> find (
			ChatUserRec userChatUser,
			ChatUserRec monitorChatUser);

}