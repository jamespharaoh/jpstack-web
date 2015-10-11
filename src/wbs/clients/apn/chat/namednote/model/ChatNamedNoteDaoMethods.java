package wbs.clients.apn.chat.namednote.model;

import java.util.List;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

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