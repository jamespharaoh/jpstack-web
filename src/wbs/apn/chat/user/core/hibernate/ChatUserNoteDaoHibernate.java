package wbs.apn.chat.user.core.hibernate;

import java.util.List;

import wbs.apn.chat.user.core.model.ChatUserNoteDao;
import wbs.apn.chat.user.core.model.ChatUserNoteRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserNoteDaoHibernate
	extends HibernateDao
	implements ChatUserNoteDao {

	@Override
	public
	List<ChatUserNoteRec> find (
			ChatUserRec chatUser) {

		return findMany (
			ChatUserNoteRec.class,

			createQuery (
				"FROM ChatUserNoteRec note " +
				"WHERE note.chatUser = :chatUser " +
				"ORDER BY note.timestamp DESC")

			.setEntity (
				"chatUser",
				chatUser)

			.list ());

	}

}
