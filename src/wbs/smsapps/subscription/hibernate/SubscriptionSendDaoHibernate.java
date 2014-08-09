package wbs.smsapps.subscription.hibernate;

import java.util.Date;

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
	SubscriptionSendRec findDue () {

		return findOne (
			SubscriptionSendRec.class,

			createQuery (
				"FROM SubscriptionSendRec ss " +
				"WHERE ss.status = :status " +
					"AND ss.scheduledForTime < :date")

			.setParameter (
				"status",
				SubscriptionStatus.scheduled,
				SubscriptionStatusType.INSTANCE)

			.setTimestamp ("date", new Date ())

			.setMaxResults (1)

			.list ());

	}

}
