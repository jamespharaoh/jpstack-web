package wbs.clients.apn.chat.core.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.List;

import org.joda.time.Interval;

import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.core.model.ChatStatsDao;
import wbs.clients.apn.chat.core.model.ChatStatsRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatStatsDaoHibernate
	extends HibernateDao
	implements ChatStatsDao {

	@Override
	public
	List<ChatStatsRec> findByTimestamp (
			ChatRec chat,
			Interval timestampInterval) {

		return findMany (
			ChatStatsRec.class,

			createQuery (
				"FROM ChatStatsRec cs " +
				"WHERE cs.chat = :chat " +
				"AND cs.timestamp >= :date1 " +
				"AND cs.timestamp <= :date2")

			.setEntity (
				"chat",
				chat)

			.setTimestamp (
				"date1",
				instantToDate (
					timestampInterval.getStart ()))

			.setTimestamp (
				"date2",
				instantToDate (
					timestampInterval.getEnd ()))

			.list ()

		);

	}

}
