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

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			@NonNull SubscriptionNumberSearch search) {

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

		if (search.subscriptionId () != null) {

			criteria.add (
				Restrictions.eq (
					"_subscription.id",
					search.subscriptionId ()));

		}

		if (search.numberLike () != null) {

			criteria.add (
				Restrictions.ilike (
					"_number.number",
					search.numberLike ()));

		}

		if (search.active () != null) {

			criteria.add (
				Restrictions.eq (
					"_subscriptionNumber.active",
					search.active ()));

		}

		if (search.joinedAfter () != null) {

			criteria.add (
				Restrictions.ge (
					"_subscriptionNumber.lastJoin",
					search.joinedAfter ()));

		}

		if (search.joinedBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"_subscriptionNumber.firstJoin",
					search.joinedBefore ()));

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
			Integer.class,
			criteria.list ());

	}

}
