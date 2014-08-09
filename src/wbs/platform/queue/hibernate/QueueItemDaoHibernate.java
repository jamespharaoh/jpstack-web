package wbs.platform.queue.hibernate;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemRec.QueueItemDaoMethods;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.user.model.UserRec;

public
class QueueItemDaoHibernate
	extends HibernateDao
	implements QueueItemDaoMethods {

	@Override
	public
	QueueItemRec findByIndex (
			QueueSubjectRec queueSubject,
			int index) {

		return findOne (
			QueueItemRec.class,

			createQuery (
				"FROM QueueItemRec queueItem " +
				"WHERE queueItem.queueSubject = :queueSubject " +
					"AND queueItem.index = :index")

			.setEntity (
				"queueSubject",
				queueSubject)

			.setInteger (
				"index",
				index)

			.list ());

	}

	@Override
	public
	List<QueueItemRec> findByCreatedTime (
			Interval createdTimeInterval) {

		return findMany (
			QueueItemRec.class,

			createQuery (
				"FROM QueueItemRec queueItem " +
				"WHERE queueItem.createdTime >= :start " +
					"AND queueItem.createdTime < :end")

			.setTimestamp (
				"start",
				createdTimeInterval.getStart ().toDate ())

			.setTimestamp (
				"end",
				createdTimeInterval.getEnd ().toDate ())

			.list ());

	}

	@Override
	public
	List<QueueItemRec> findByProcessedTime (
			Interval processedTimeInterval) {

		return findMany (
			QueueItemRec.class,

			createQuery (
				"FROM QueueItemRec queueItem " +
				"WHERE queueItem.processedTime >= :start " +
					"AND queueItem.processedTime < :end")

			.setTimestamp (
				"start",
				processedTimeInterval.getStart ().toDate ())

			.setTimestamp (
				"end",
				processedTimeInterval.getEnd ().toDate ())

			.list ());

	}

	@Override
	public
	List<QueueItemRec> findByProcessedTime (
			UserRec user,
			Interval processedTimeInterval) {

		return findMany (
			QueueItemRec.class,

			createQuery (
				"FROM QueueItemRec queueItem " +
				"WHERE queueItem.processedUser = :user " +
					"AND queueItem.processedTime >= :startTime " +
					"AND queueItem.processedTime < :endTime")

			.setEntity (
				"user",
				user)

			.setTimestamp (
				"startTime",
				processedTimeInterval.getStart ().toDate ())

			.setTimestamp (
				"endTime",
				processedTimeInterval.getEnd ().toDate ())

			.list ());

	}

}
