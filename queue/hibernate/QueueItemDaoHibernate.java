package wbs.platform.queue.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.parseInterval;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.queue.model.QueueItemDao;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemSearch;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.user.model.UserRec;

public
class QueueItemDaoHibernate
	extends HibernateDao
	implements QueueItemDao {

	@Override
	public
	List<Integer> searchIds (
			@NonNull QueueItemSearch search) {

		Criteria criteria =
			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.createAlias (
				"_queueItem.queueSubject",
				"_queueSubject")

			.createAlias (
				"_queueSubject.queue",
				"_queue")

			.createAlias (
				"_queueItem.processedUser",
				"_processedUser");

		if (
			isNotNull (
				search.createdTime ())
		) {

			Interval createdTimeInterval =
				parseInterval (
					DateTimeZone.getDefault (),
					search.createdTime ());

			criteria.add (
				Restrictions.ge (
					"_queueItem.createdTime",
					instantToDate (
						createdTimeInterval.getStart ())));

			criteria.add (
				Restrictions.lt (
					"_queueItem.createdTime",
					instantToDate (
						createdTimeInterval.getEnd ())));

		}

		if (
			isNotNull (
				search.processedUserId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_processedUser.id",
					search.processedUserId ()));

		}

		if (search.filter ()) {

			List<Criterion> filterCriteria =
				new ArrayList<Criterion> ();

			if (
				isNotEmpty (
					search.filterQueueIds ())
			) {

				filterCriteria.add (
					Restrictions.in (
						"_queue.id",
						search.filterQueueIds ()));

			}

			criteria.add (
				Restrictions.or (
					filterCriteria.toArray (
						new Criterion [] {})));

		}

		criteria.setProjection (
			Projections.id ());

		return findMany (
			Integer.class,
			criteria.list ());

	}

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
			@NonNull Interval createdTimeInterval) {

		return findMany (
			QueueItemRec.class,

			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.add (
				Restrictions.ge (
					"_queueItem.createdTime",
					instantToDate (
						createdTimeInterval.getStart ().toInstant ())))

			.add (
				Restrictions.lt (
					"_queueItem.createdTime",
					instantToDate (
						createdTimeInterval.getEnd ().toInstant ())))

			.list ());

	}

	@Override
	public
	List<QueueItemRec> findByCreatedTime (
			@NonNull QueueRec queue,
			@NonNull Interval createdTimeInterval) {

		return findMany (
			QueueItemRec.class,

			createCriteria (
				QueueItemRec.class,
				"_queueItem")

			.createAlias (
				"_queueItem.queueSubject",
				"_queueSubject")

			.add (
				Restrictions.eq (
					"_queueSubject.queue",
					queue))

			.add (
				Restrictions.ge (
					"_queueItem.createdTime",
					instantToDate (
						createdTimeInterval.getStart ().toInstant ())))

			.add (
				Restrictions.lt (
					"_queueItem.createdTime",
					instantToDate (
						createdTimeInterval.getEnd ().toInstant ())))

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
