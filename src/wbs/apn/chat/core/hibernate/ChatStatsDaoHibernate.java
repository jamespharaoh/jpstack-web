package wbs.apn.chat.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.core.model.ChatStatsDao;
import wbs.apn.chat.core.model.ChatStatsRec;

public
class ChatStatsDaoHibernate
	extends HibernateDao
	implements ChatStatsDao {

	// singeton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ChatStatsRec> findByTimestamp (
			@NonNull Transaction parentTransaction,
			@NonNull ChatRec chat,
			@NonNull Interval timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByTimestamp");

		) {

			return findMany (
				transaction,
				ChatStatsRec.class,

				createCriteria (
					transaction,
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

}
