package wbs.smsapps.subscription.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.subscription.model.SubscriptionSendDao;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSendState;

public
class SubscriptionSendDaoHibernate
	extends HibernateDao
	implements SubscriptionSendDao {

	public
	List<SubscriptionSendRec> findSending () {

		return findMany (
			SubscriptionSendRec.class,

			createCriteria (
				SubscriptionSendRec.class,
				"_subscriptionSend")

			.add (
				Restrictions.eq (
					"_subscriptionSend.state",
					SubscriptionSendState.sending))

			.list ());

	}

	@Override
	public
	List<SubscriptionSendRec> findScheduled (
			Instant now) {

		return findMany (
			SubscriptionSendRec.class,

			createCriteria (
				SubscriptionSendRec.class,
				"_subscriptionSend")

			.add (
				Restrictions.eq (
					"_subscriptionSend.state",
					SubscriptionSendState.scheduled))

			.add (
				Restrictions.le (
					"_subscriptionSend.scheduledForTime",
					now))

			.list ());

	}

}
