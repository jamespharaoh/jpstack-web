package wbs.clients.apn.chat.bill.hibernate;

import java.util.List;

import org.joda.time.Interval;

import wbs.clients.apn.chat.bill.model.ChatUserBillLogDao;
import wbs.clients.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.hibernate.TimestampWithTimezoneUserType;

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

			.setParameter (
				"from",
				timestampInterval.getStart (),
				TimestampWithTimezoneUserType.INSTANCE)

			.setParameter (
				"to",
				timestampInterval.getEnd (),
				TimestampWithTimezoneUserType.INSTANCE)

			.list ()

		);

	}

}
