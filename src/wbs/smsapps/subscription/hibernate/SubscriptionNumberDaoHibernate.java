package wbs.smsapps.subscription.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.subscription.model.SubscriptionNumberDao;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionNumberSearch;
import wbs.smsapps.subscription.model.SubscriptionRec;

public
class SubscriptionNumberDaoHibernate
	extends HibernateDao
	implements SubscriptionNumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	SubscriptionNumberRec find (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				SubscriptionNumberRec.class,

				createCriteria (
					transaction,
					SubscriptionNumberRec.class,
					"_subscriptionNumber")

				.add (
					Restrictions.eq (
						"_subscriptionNumber.subscription",
						subscription))

				.add (
					Restrictions.eq (
						"_subscriptionNumber.number",
						number))

			);

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionNumberSearch subscriptionNumberSearch) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			// create criteria

			Criteria criteria =

				createCriteria (
					transaction,
					SubscriptionNumberRec.class,
					"_subscriptionNumber")

				.createAlias (
					"_subscriptionNumber.subscription",
					"_subscription")

				.createAlias (
					"_subscriptionNumber.number",
					"_number");

			// apply filters

			if (subscriptionNumberSearch.subscriptionId () != null) {

				criteria.add (
					Restrictions.eq (
						"_subscription.id",
						subscriptionNumberSearch.subscriptionId ()));

			}

			if (subscriptionNumberSearch.numberLike () != null) {

				criteria.add (
					Restrictions.ilike (
						"_number.number",
						subscriptionNumberSearch.numberLike ()));

			}

			if (subscriptionNumberSearch.active () != null) {

				criteria.add (
					Restrictions.eq (
						"_subscriptionNumber.active",
						subscriptionNumberSearch.active ()));

			}

			if (subscriptionNumberSearch.joinedAfter () != null) {

				criteria.add (
					Restrictions.ge (
						"_subscriptionNumber.lastJoin",
						subscriptionNumberSearch.joinedAfter ()));

			}

			if (subscriptionNumberSearch.joinedBefore () != null) {

				criteria.add (
					Restrictions.lt (
						"_subscriptionNumber.firstJoin",
						subscriptionNumberSearch.joinedBefore ()));

			}

			// add default order

			criteria

				.addOrder (
					Order.asc ("number"));

			// set to return ids only

			criteria

				.setProjection (
					Projections.id ());

			// perform and return

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
