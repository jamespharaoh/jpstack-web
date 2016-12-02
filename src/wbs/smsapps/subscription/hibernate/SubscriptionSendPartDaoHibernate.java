package wbs.smsapps.subscription.hibernate;

import lombok.NonNull;

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
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull SubscriptionListRec subscriptionList) {

		return findOneOrNull (
			"find (subscriptionSend, subscriptionList)",
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

		);

	}

}
