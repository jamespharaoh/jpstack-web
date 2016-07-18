package wbs.clients.apn.chat.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
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
			@NonNull ChatRec chat,
			@NonNull Interval timestamp) {

		return findMany (
			"findByTimestamp (chat, timestamp)",
			ChatStatsRec.class,

			createCriteria (
				ChatStatsRec.class,
				"_chatStats")

			.add (
				Restrictions.eq (
					"_chatStats.chat",
					chat))

			.add (
				Restrictions.ge (
					"_chatStats.timestamp",
					timestamp.getStart ()))

			.add (
				Restrictions.lt (
					"_chatStats.timestamp",
					timestamp.getEnd ()))

		);

	}

}
