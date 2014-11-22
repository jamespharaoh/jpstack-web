package wbs.smsapps.subscription.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.subscription.model.SubscriptionSendDao;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionStatus;

public
class SubscriptionSendDaoHibernate
	extends HibernateDao
	implements SubscriptionSendDao {

	@Override
	public
	SubscriptionSendRec findDue (
			Instant now) {

		return findOne (
			SubscriptionSendRec.class,

			createCriteria (
				SubscriptionSendRec.class,
				"_subscriptionSend")

			.add (
				Restrictions.eq (
					"_subscriptionSend.status",
					SubscriptionStatus.scheduled))

			.add (
				Restrictions.le (
					"_subscriptionSend.scheduledForTime",
					instantToDate (now)))

			.setMaxResults (1)

			.list ());

	}

}
