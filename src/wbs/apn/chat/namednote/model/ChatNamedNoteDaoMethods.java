package wbs.apn.chat.namednote.model;

import java.util.List;

import wbs.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatNamedNoteDaoMethods {

	ChatNamedNoteRec find (
			ChatUserRec thisChatUser,
			ChatUserRec otherChatUser,
			ChatNoteNameRec chatNoteName);

	List<ChatNamedNoteRec> find (
			ChatUserRec thisChatUser,
			ChatUserRec otherChatUser);

}