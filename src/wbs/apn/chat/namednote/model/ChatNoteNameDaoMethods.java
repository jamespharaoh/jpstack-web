package wbs.apn.chat.namednote.model;

import java.util.List;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;

public
interface ChatNoteNameDaoMethods {

	List<ChatNoteNameRec> findNotDeleted (
			ChatRec chat);

}