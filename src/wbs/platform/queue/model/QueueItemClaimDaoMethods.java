package wbs.platform.queue.model;

import java.util.List;

import wbs.platform.user.model.UserRec;

public
interface QueueItemClaimDaoMethods {

	List<QueueItemClaimRec> findClaimed ();

	List<QueueItemClaimRec> findClaimed (
			UserRec user);

}