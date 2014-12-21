package wbs.smsapps.subscription.hibernate;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionSendPartDao;
import wbs.smsapps.subscription.model.SubscriptionSendPartRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
class SubscriptionSendPartDaoHibernate
	extends HibernateDao
	implements SubscriptionSendPartDao {

	@Override
	public
	SubscriptionSendPartRec find (
			SubscriptionSendRec subscriptionSend,
			SubscriptionListRec subscriptionList) {

		return findOne (
			SubscriptionSendPartRec.class,

			createCriteria (
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

			.list ());

	}

}
