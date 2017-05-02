package wbs.apn.chat.namednote.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatNamedNoteDaoMethods {

	ChatNamedNoteRec find (
			Transaction parentTransaction,
			ChatUserRec thisChatUser,
			ChatUserRec otherChatUser,
			ChatNoteNameRec chatNoteName);

	List <ChatNamedNoteRec> find (
			Transaction parentTransaction,
			ChatUserRec thisChatUser,
			ChatUserRec otherChatUser);

}