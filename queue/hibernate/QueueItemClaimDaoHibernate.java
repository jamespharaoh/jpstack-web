package wbs.platform.queue.hibernate;

import java.util.List;

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
			QueueItemClaimRec.class,

			createQuery (
				"FROM QueueItemClaimRec queueItemClaim " +
				"WHERE queueItemClaim.status = :claimedStatus")

			.setParameter (
				"claimedStatus",
				QueueItemClaimStatus.claimed,
				QueueItemClaimStatusType.INSTANCE)

			.list ());

	}

	@Override
	public
	List<QueueItemClaimRec> findClaimed (
			UserRec user) {

		return findMany (
			QueueItemClaimRec.class,

			createQuery (
				"FROM QueueItemClaimRec queueItemClaim " +
				"WHERE queueItemClaim.user = :user " +
					"AND queueItemClaim.status = :claimedStatus")

			.setEntity (
				"user",
				user)

			.setParameter (
				"claimedStatus",
				QueueItemClaimStatus.claimed,
				QueueItemClaimStatusType.INSTANCE)

			.list ());

	}

}
