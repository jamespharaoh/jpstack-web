package wbs.platform.queue.model;

import java.util.List;

import org.hibernate.Criteria;
import org.joda.time.Interval;

import wbs.platform.user.model.UserRec;

public
interface QueueItemDaoMethods {

	Criteria searchCriteria (
			QueueItemSearch search);

	List<Integer> searchIds (
			QueueItemSearch search);

	Criteria searchUserQueueReportCriteria (
			QueueItemSearch search);

	List<Integer> searchUserQueueReportIds (
			QueueItemSearch search);

	List<UserQueueReport> searchUserQueueReports (
			QueueItemSearch search,
			List<Integer> ids);

	List<QueueItemRec> find (
			List<QueueItemState> state);

	List<QueueItemRec> findByCreatedTime (
			Interval createdTimeInterval);

	List<QueueItemRec> findByCreatedTime (
			QueueRec queue,
			Interval createdTimeInterval);

	List<QueueItemRec> findByProcessedTime (
			Interval processedTimeInterval);

	List<QueueItemRec> findByProcessedTime (
			UserRec user,
			Interval processedTimeInterval);

}