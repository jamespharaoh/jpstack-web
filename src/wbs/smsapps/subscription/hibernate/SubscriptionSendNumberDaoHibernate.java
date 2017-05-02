package wbs.smsapps.subscription.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.smsapps.subscription.model.SubscriptionSendNumberDao;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberState;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
class SubscriptionSendNumberDaoHibernate
	extends HibernateDao
	implements SubscriptionSendNumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <SubscriptionSendNumberRec> findQueuedLimit (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findQueuedLimit");

		) {

			return findMany (
				transaction,
				SubscriptionSendNumberRec.class,

				createCriteria (
					transaction,
					SubscriptionSendNumberRec.class,
					"_subscriptionSendNumber")

				.add (
					Restrictions.eq (
						"_subscriptionSendNumber.subscriptionSend",
						subscriptionSend))

				.add (
					Restrictions.eq (
						"_subscriptionSendNumber.state",
						SubscriptionSendNumberState.queued))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
