package wbs.clients.apn.chat.bill.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
			@NonNull ChatUserRec chatUser,
			@NonNull Interval timestamp) {

		return findMany (
			"findByTimestamp (chatUser, timestampInterval)",
			ChatUserBillLogRec.class,

			createCriteria (
				ChatUserBillLogRec.class,
				"_chatUserBillLog")

			.add (
				Restrictions.eq (
					"_chatUserBillLog.chatUser",
					chatUser))

			.add (
				Restrictions.ge (
					"_chatUserBillLog.timestamp",
					timestamp.getStart ()))

			.add (
				Restrictions.lt (
					"_chatUserBillLog.timestamp",
					timestamp.getEnd ()))

			.addOrder (
				Order.desc (
					"_chatUserBillLog.timestamp"))

		);

	}

}
