package wbs.platform.queue.model;

import java.util.List;

import org.joda.time.Interval;

import wbs.platform.user.model.UserRec;

public
interface QueueItemDaoMethods {

	List<Integer> searchIds (
			QueueItemSearch search);

	QueueItemRec findByIndex (
			QueueSubjectRec queueSubject,
			int index);

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