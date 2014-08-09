package wbs.apn.chat.bill.hibernate;

import java.util.List;

import org.joda.time.Interval;

import wbs.apn.chat.bill.model.ChatUserBillLogDao;
import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;

public
class ChatUserBillLogDaoHibernate
	extends HibernateDao
	implements ChatUserBillLogDao {

	@Override
	public
	List<ChatUserBillLogRec> findByTimestamp (
			ChatUserRec chatUser,
			Interval timestampInterval) {

		return findMany (
			ChatUserBillLogRec.class,

			createQuery (
				"FROM ChatUserBillLogRec chatUserBillLog " +
				"WHERE chatUserBillLog.chatUser = :chatUser " +
					"AND chatUserBillLog.timestamp >= :from " +
					"AND chatUserBillLog.timestamp < :to " +
				"ORDER BY chatUserBillLog.timestamp DESC")

			.setEntity (
				"chatUser",
				chatUser)

			.setTimestamp (
				"from",
				timestampInterval.getStart ().toDate ())

			.setTimestamp (
				"to",
				timestampInterval.getEnd ().toDate ())

			.list ());

	}

}
