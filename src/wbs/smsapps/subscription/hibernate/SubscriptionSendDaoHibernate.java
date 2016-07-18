package wbs.smsapps.subscription.hibernate;

import java.util.List;

import lombok.NonNull;

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

	@Override
	public
	List<SubscriptionSendRec> findSending () {

		return findMany (
			"findSending ()",
			SubscriptionSendRec.class,

			createCriteria (
				SubscriptionSendRec.class,
				"_subscriptionSend")

			.add (
				Restrictions.eq (
					"_subscriptionSend.state",
					SubscriptionSendState.sending))

		);

	}

	@Override
	public
	List<SubscriptionSendRec> findScheduled (
			@NonNull Instant now) {

		return findMany (
			"findScheduled (now)",
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

		);

	}

}
