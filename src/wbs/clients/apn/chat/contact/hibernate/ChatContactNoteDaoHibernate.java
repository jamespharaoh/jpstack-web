package wbs.clients.apn.chat.contact.hibernate;

import java.util.List;

import org.joda.time.Interval;

import wbs.clients.apn.chat.contact.model.ChatContactNoteDao;
import wbs.clients.apn.chat.contact.model.ChatContactNoteRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatContactNoteDaoHibernate
	extends HibernateDao
	implements ChatContactNoteDao {

	@Override
	public
	List<ChatContactNoteRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval) {

		return findMany (
			ChatContactNoteRec.class,

			createQuery (
				"FROM ChatContactNoteRec note " +
				"WHERE note.chat = :chat " +
					"AND note.timestamp >= :start " +
					"AND note.timestamp < :end " +
				"ORDER BY note.timestamp")

			.setEntity (
				"chat",
				chat)

			.setTimestamp (
				"start",
				timestampInterval.getStart ().toDate ())

			.setTimestamp (
				"end",
				timestampInterval.getEnd ().toDate ())

			.list ());

	}

	@Override
	public
	List<ChatContactNoteRec> find (
			ChatUserRec userChatUser,
			ChatUserRec monitorChatUser) {

		return findMany (
			ChatContactNoteRec.class,

			createQuery (
				"FROM ChatContactNoteRec note " +
				"WHERE note.user = :userChatUser " +
					"AND note.monitor = :monitorChatUser " +
				"ORDER BY " +
					"note.pegged DESC, " +
					"note.timestamp")

			.setEntity (
				"userChatUser",
				userChatUser)

			.setEntity (
				"monitorChatUser",
				monitorChatUser)

			.list ());

	}


}
