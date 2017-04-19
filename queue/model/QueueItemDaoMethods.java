package wbs.platform.queue.model;

import java.util.List;

import com.google.common.base.Optional;

import org.hibernate.Criteria;
import org.joda.time.Interval;

import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserRec;

public
interface QueueItemDaoMethods {

	Criteria searchCriteria (
			TaskLogger parentTaskLogger,
			QueueItemSearch search);

	List <Long> searchIds (
			TaskLogger parentTaskLogger,
			QueueItemSearch search);

	Criteria searchUserQueueReportCriteria (
			TaskLogger parentTaskLogger,
			QueueItemSearch search);

	List <Long> searchUserQueueReportIds (
			TaskLogger parentTaskLogger,
			QueueItemSearch search);

	List <Optional <UserQueueReport>> searchUserQueueReports (
			TaskLogger parentTaskLogger,
			QueueItemSearch search,
			List <Long> ids);

	List <QueueItemRec> find (
			List <QueueItemState> state);

	List <QueueItemRec> findByCreatedTime (
			Interval createdTimeInterval);

	List <QueueItemRec> findByCreatedTime (
			QueueRec queue,
			Interval createdTimeInterval);

	List <QueueItemRec> findByProcessedTime (
			Interval processedTimeInterval);

	List <QueueItemRec> findByProcessedTime (
			UserRec user,
			Interval processedTimeInterval);

}