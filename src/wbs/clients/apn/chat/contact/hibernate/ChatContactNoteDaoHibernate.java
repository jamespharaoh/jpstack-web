package wbs.clients.apn.chat.contact.hibernate;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Interval;

import wbs.clients.apn.chat.contact.model.ChatContactNoteDao;
import wbs.clients.apn.chat.contact.model.ChatContactNoteRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.hibernate.TimestampWithTimezoneUserType;

public
class ChatContactNoteDaoHibernate
	extends HibernateDao
	implements ChatContactNoteDao {

	@Override
	public
	List<ChatContactNoteRec> findByTimestamp (
			@NonNull ChatRec chat,
			@NonNull Interval timestampInterval) {

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

			.setParameter (
				"start",
				timestampInterval.getStart (),
				TimestampWithTimezoneUserType.INSTANCE)

			.setParameter (
				"end",
				timestampInterval.getEnd (),
				TimestampWithTimezoneUserType.INSTANCE)

			.list ()

		);

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
