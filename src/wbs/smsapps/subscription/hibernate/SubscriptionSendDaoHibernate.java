package wbs.smsapps.subscription.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.smsapps.subscription.model.SubscriptionSendDao;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSendState;

public
class SubscriptionSendDaoHibernate
	extends HibernateDao
	implements SubscriptionSendDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <SubscriptionSendRec> findSending (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findSending");

		) {

			return findMany (
				transaction,
				SubscriptionSendRec.class,

				createCriteria (
					transaction,
					SubscriptionSendRec.class,
					"_subscriptionSend")

				.add (
					Restrictions.eq (
						"_subscriptionSend.state",
						SubscriptionSendState.sending))

			);

		}

	}

	@Override
	public
	List <SubscriptionSendRec> findScheduled (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findScheduled");

		) {

			return findMany (
				transaction,
				SubscriptionSendRec.class,

				createCriteria (
					transaction,
					SubscriptionSendRec.class,
					"_subscriptionSend")

				.add (
					Restrictions.eq (
						"_subscriptionSend.state",
						SubscriptionSendState.scheduled))

				.add (
					Restrictions.le (
						"_subscriptionSend.scheduledForTime",
						now))

			);

		}

	}

}
