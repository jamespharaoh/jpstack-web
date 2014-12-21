package wbs.smsapps.subscription.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.subscription.model.SubscriptionSubDao;

public
class SubscriptionSubDaoHibernate
	extends HibernateDao
	implements SubscriptionSubDao {

	/*
	@Override
	public
	SubscriptionSubRec findActive (
			SubscriptionRec subscription,
			NumberRec number) {

		return findOne (
			SubscriptionSubRec.class,

			createCriteria (
				SubscriptionSubRec.class,
				"_subscriptionSub")

			.add (
				Restrictions.eq (
					"_subscriptionSub.subscription",
					subscription))

			.add (
				Restrictions.eq (
					"_subscriptionSub.number",
					number))

			.add (
				Restrictions.eq (
					"_subscriptionSub.active",
					true))

			.list ());

	}

	@Override
	public
	List<SubscriptionSubRec> findActive (
			SubscriptionRec subscription) {

		return findMany (
			SubscriptionSubRec.class,

			createCriteria (
				SubscriptionSubRec.class,
				"_subscriptionSub")

			.add (
				Restrictions.eq (
					"_subscriptionSub.subscription",
					subscription))

			.add (
				Restrictions.eq (
					"_subscriptionSub.active",
					true))

			.list ());

	}
	*/

}
