package wbs.smsapps.broadcast.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.smsapps.broadcast.model.BroadcastDao;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

public
class BroadcastDaoHibernate
	extends HibernateDao
	implements BroadcastDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <BroadcastRec> findSending (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findSending");

		) {

			return findMany (
				transaction,
				BroadcastRec.class,

				createCriteria (
					transaction,
					BroadcastRec.class,
					"_broadcast")

				.add (
					Restrictions.eq (
						"_broadcast.state",
						BroadcastState.sending))

			);

		}

	}

	@Override
	public
	List <BroadcastRec> findScheduled (
			@NonNull Transaction parentTransaction,
			@NonNull Instant scheduledTime) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findScheduled");

		) {

			return findMany (
				transaction,
				BroadcastRec.class,

				createCriteria (
					transaction,
					BroadcastRec.class,
					"_broadcast")

				.add (
					Restrictions.eq (
						"_broadcast.state",
						BroadcastState.scheduled))

				.add (
					Restrictions.le (
						"_broadcast.scheduledTime",
						scheduledTime))

			);

		}

	}

}
