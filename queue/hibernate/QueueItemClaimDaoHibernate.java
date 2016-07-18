package wbs.platform.queue.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.queue.model.QueueItemClaimDaoMethods;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemClaimStatus;
import wbs.platform.user.model.UserRec;

public
class QueueItemClaimDaoHibernate
	extends HibernateDao
	implements QueueItemClaimDaoMethods {

	@Override
	public
	List<QueueItemClaimRec> findClaimed () {

		return findMany (
			"findClaimed ()",
			QueueItemClaimRec.class,

			createCriteria (
				QueueItemClaimRec.class)

			.add (
				Restrictions.eq (
					"status",
					QueueItemClaimStatus.claimed))

		);

	}

	@Override
	public
	List<QueueItemClaimRec> findClaimed (
			@NonNull UserRec user) {

		return findMany (
			"findClaimed (user)",
			QueueItemClaimRec.class,

			createCriteria (
				QueueItemClaimRec.class)

			.add (
				Restrictions.eq (
					"user",
					user))

			.add (
				Restrictions.eq (
					"status",
					QueueItemClaimStatus.claimed))

		);

	}

}
