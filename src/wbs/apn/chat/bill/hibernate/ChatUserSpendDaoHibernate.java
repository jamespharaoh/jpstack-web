package wbs.apn.chat.bill.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.bill.model.ChatUserSpendDao;
import wbs.apn.chat.bill.model.ChatUserSpendRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
class ChatUserSpendDaoHibernate
	extends HibernateDao
	implements ChatUserSpendDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ChatUserSpendRec findByDate (
			@NonNull Transaction parentTransaction,
			@NonNull ChatUserRec chatUser,
			@NonNull LocalDate date) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByDate");

		) {

			return findOneOrNull (
				transaction,
				ChatUserSpendRec.class,

				createCriteria (
					transaction,
					ChatUserSpendRec.class)

				.add (
					Restrictions.eq (
						"chatUser",
						chatUser))

				.add (
					Restrictions.eq (
						"date",
						date))

			);

		}

	}

}
