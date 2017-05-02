package wbs.apn.chat.contact.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.database.Transaction;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactNoteDaoMethods {

	List <ChatContactNoteRec> findByTimestamp (
			Transaction parentTransaction,
			ChatRec chat,
			Interval timestampInterval);

	List <ChatContactNoteRec> find (
			Transaction parentTransaction,
			ChatUserRec userChatUser,
			ChatUserRec monitorChatUser);

}