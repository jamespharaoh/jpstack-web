package wbs.apn.chat.contact.hibernate;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.contact.model.ChatUserInitiationLogDao;
import wbs.apn.chat.contact.model.ChatUserInitiationLogRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserInitiationLogDaoHibernate
	extends HibernateDao
	implements ChatUserInitiationLogDao {

	@Override
	public
	List<ChatUserInitiationLogRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval) {

		return findMany (
			ChatUserInitiationLogRec.class,

			createQuery (
				"FROM ChatUserInitiationLogRec log " +
				"WHERE log.chatUser.chat = :chat " +
					"AND log.timestamp >= :startTime " +
					"AND log.timestamp < :endTime")

			.setEntity (
				"chat",
				chat)

			.setTimestamp (
				"startTime",
				timestampInterval.getStart ().toDate ())

			.setDate (
				"endTime",
				timestampInterval.getEnd ().toDate ())

			.list ());

	}

}
