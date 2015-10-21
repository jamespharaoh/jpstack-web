package wbs.clients.apn.chat.bill.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.List;

import org.joda.time.Interval;

import wbs.clients.apn.chat.bill.model.ChatUserBillLogDao;
import wbs.clients.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
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
				instantToDate (
					timestampInterval.getStart ()))

			.setTimestamp (
				"to",
				instantToDate (
					timestampInterval.getEnd ()))

			.list ()

		);

	}

}
