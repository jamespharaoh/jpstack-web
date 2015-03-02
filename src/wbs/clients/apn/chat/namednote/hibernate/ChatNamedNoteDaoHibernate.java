package wbs.clients.apn.chat.namednote.hibernate;

import java.util.List;

import wbs.clients.apn.chat.namednote.model.ChatNamedNoteDao;
import wbs.clients.apn.chat.namednote.model.ChatNamedNoteRec;
import wbs.clients.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;

@SingletonComponent ("chatNamedNoteDaoHibernate")
public
 class ChatNamedNoteDaoHibernate
	extends HibernateDao
	implements ChatNamedNoteDao {

	@Override
	public
	ChatNamedNoteRec find (
			ChatUserRec thisChatUser,
			ChatUserRec otherChatUser,
			ChatNoteNameRec chatNoteName) {

		return findOne (
			ChatNamedNoteRec.class,

			createQuery (
				"FROM ChatNamedNoteRec namedNote " +
				"WHERE namedNote.thisUser = :thisChatUser " +
					"AND namedNote.otherUser = :otherChatUser " +
					"AND namedNote.chatNoteName = :chatNoteName")

			.setEntity (
				"thisChatUser",
				thisChatUser)

			.setEntity (
				"otherChatUser",
				otherChatUser)

			.setEntity (
				"chatNoteName",
				chatNoteName)

			.list ());

	}

	@Override
	public
	List<ChatNamedNoteRec> find (
			ChatUserRec thisChatUser,
			ChatUserRec otherChatUser) {

		return findMany (
			ChatNamedNoteRec.class,

			createQuery (
				"FROM ChatNamedNoteRec namedNote " +
				"WHERE namedNote.thisUser = :thisChatUser " +
					"AND namedNote.otherUser = :otherChatUser")

			.setEntity (
				"thisChatUser",
				thisChatUser)

			.setEntity (
				"otherChatUser",
				otherChatUser)

			.list ());

	}

}
