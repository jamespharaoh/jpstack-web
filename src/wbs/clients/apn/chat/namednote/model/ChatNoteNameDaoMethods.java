package wbs.clients.apn.chat.namednote.model;

import java.util.List;

import wbs.clients.apn.chat.core.model.ChatRec;

public
interface ChatNoteNameDaoMethods {

	List<ChatNoteNameRec> findNotDeleted (
			ChatRec chat);

}