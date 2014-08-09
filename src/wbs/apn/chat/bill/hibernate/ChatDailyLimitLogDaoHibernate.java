package wbs.apn.chat.bill.hibernate;

import java.util.List;

import wbs.apn.chat.bill.model.ChatDailyLimitLogDao;
import wbs.apn.chat.bill.model.ChatDailyLimitLogRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatDailyLimitLogDaoHibernate
	extends HibernateDao
	implements ChatDailyLimitLogDao {

	@Override
	public
	List<ChatDailyLimitLogRec> findLimitedToday () {

		// TODO wtf is going on here?

		return findMany (
			ChatDailyLimitLogRec.class,

			createQuery (
				"FROM ChatDailyLimitLogRec rec " +
				"WHERE time >= 'today'")

			.list ());

	}

}
