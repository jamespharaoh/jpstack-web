package wbs.smsapps.subscription.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.model.SubscriptionNumberDao;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionNumberSearch;
import wbs.smsapps.subscription.model.SubscriptionRec;

public
class SubscriptionNumberDaoHibernate
	extends HibernateDao
	implements SubscriptionNumberDao {

	@Override
	public
	SubscriptionNumberRec find (
			@NonNull SubscriptionRec subscription,
			@NonNull NumberRec number) {

		return findOne (
			"find (subscription, number)",
			SubscriptionNumberRec.class,

			createCriteria (
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

	@Override
	public
	List<Integer> searchIds (
			@NonNull SubscriptionNumberSearch subscriptionNumberSearch) {

		// create criteria

		Criteria criteria =

			createCriteria (
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
			"searchIds (subscriptionNumberSearch)",
			Integer.class,
			criteria);

	}

}
