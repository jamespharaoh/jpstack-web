package wbs.smsapps.subscription.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionSendPartDao;
import wbs.smsapps.subscription.model.SubscriptionSendPartRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
class SubscriptionSendPartDaoHibernate
	extends HibernateDao
	implements SubscriptionSendPartDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	SubscriptionSendPartRec find (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull SubscriptionListRec subscriptionList) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				SubscriptionSendPartRec.class,

				createCriteria (
					transaction,
					SubscriptionSendPartRec.class,
					"_subscriptionSendPart")

				.add (
					Restrictions.eq (
						"_subscriptionSendPart.subscriptionSend",
						subscriptionSend))

				.add (
					Restrictions.eq (
						"_subscriptionSendPart.subscriptionList",
						subscriptionList))

			);

		}

	}

}
