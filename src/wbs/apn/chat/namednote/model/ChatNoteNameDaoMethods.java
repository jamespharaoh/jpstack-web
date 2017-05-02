package wbs.apn.chat.namednote.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.apn.chat.core.model.ChatRec;

public
interface ChatNoteNameDaoMethods {

	List <ChatNoteNameRec> findNotDeleted (
			Transaction parentTransaction,
			ChatRec chat);

}