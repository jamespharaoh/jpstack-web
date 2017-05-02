package wbs.apn.chat.bill.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.bill.model.ChatUserBillLogDao;
import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatUserBillLogDaoHibernate
	extends HibernateDao
	implements ChatUserBillLogDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatUserBillLogRec> findByTimestamp (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull Interval timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByTimestamp");

		) {

			return findMany (
				transaction,
				ChatUserBillLogRec.class,

				createCriteria (
					transaction,
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

}
