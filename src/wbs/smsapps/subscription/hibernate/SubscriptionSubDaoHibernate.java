package wbs.smsapps.subscription.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSubDao;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

public
class SubscriptionSubDaoHibernate
	extends HibernateDao
	implements SubscriptionSubDao {

	@Override
	public
	SubscriptionSubRec findActive (
			SubscriptionRec subscription,
			NumberRec number) {

		return findOne (
			SubscriptionSubRec.class,

			createQuery (
				"FROM SubscriptionSubRec ss " +
				"WHERE ss.subscription = :subscription " +
					"AND ss.number = :number " +
					"AND ss.active = true")

			.setEntity (
				"subscription",
				subscription)

			.setEntity (
				"number",
				number)

			.list ());

	}

	@Override
	public
	List<SubscriptionSubRec> findActive (
			SubscriptionRec subscription) {

		return findMany (
			SubscriptionSubRec.class,

			createQuery (
				"FROM SubscriptionSubRec ss " +
				"WHERE ss.subscription = :subscription " +
					"AND ss.active = true")

			.setEntity (
				"subscription",
				subscription)

			.list ());

	}

}
