package wbs.apn.chat.user.core.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ChatUserNoteDaoMethods {

	List <ChatUserNoteRec> find (
			Transaction parentTransaction,
			ChatUserRec chatUser);

}