package wbs.apn.chat.namednote.hibernate;

import java.util.List;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.namednote.model.ChatNoteNameDao;
import wbs.apn.chat.namednote.model.ChatNoteNameRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatNoteNameDaoHibernate
	extends HibernateDao
	implements ChatNoteNameDao {

	@Override
	public
	List<ChatNoteNameRec> findNotDeleted (
			ChatRec chat) {

		return findMany (
			ChatNoteNameRec.class,

			createQuery (
				"FROM ChatNoteNameRec noteName " +
				"WHERE noteName.chat = :chat " +
					"AND noteName.deleted = :deleted " +
				"ORDER BY noteName.index")

			.setEntity (
				"chat",
				chat)

			.setBoolean (
				"deleted",
				false)

			.list ());

	}

}
