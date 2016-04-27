package wbs.platform.exception.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.exception.model.ExceptionLogDao;
import wbs.platform.exception.model.ExceptionLogRec;
import wbs.platform.exception.model.ExceptionLogSearch;
import wbs.platform.exception.model.ExceptionLogTypeRec;
import wbs.platform.user.model.UserRec;

public
class ExceptionLogDaoHibernate
	extends HibernateDao
	implements ExceptionLogDao {

	@Override
	public
	int countWithAlert () {

		return (int) (long) findOne (
			Long.class,

			createQuery (
				"SELECT count (*) " +
				"FROM ExceptionLogRec AS e " +
				"WHERE e.alert = true")

			.list ());

	}

	@Override
	public
	int countWithAlertAndFatal () {

		return (int) (long) findOne (
			Long.class,

			createQuery (
				"SELECT count (*) " +
				"FROM ExceptionLogRec AS exception " +
				"WHERE exception.alert = true " +
				"AND exception.fatal = true")

			.list ());

	}

	@Override
	public
	List<Integer> searchIds (
			ExceptionLogSearch search) {

		Criteria criteria =
			createCriteria (
				ExceptionLogRec.class);

		if (search.alert () != null) {

			criteria.add (
				Restrictions.eq (
					"alert",
					search.alert ()));

		}

		if (search.fatal () != null) {

			criteria.add (
				Restrictions.eq (
					"fatal",
					search.fatal ()));

		}

		if (search.typeId () != null) {

			ExceptionLogTypeRec type =
				get (
					ExceptionLogTypeRec.class,
					search.typeId ());

			criteria.add (
				Restrictions.eq (
					"type",
					type));

		}

		if (search.userId () != null) {

			UserRec user =
				get (
					UserRec.class,
					search.userId ());

			criteria.add (
				Restrictions.eq (
					"user",
					user));

		}

		if (search.order () != null) {

			switch (search.order ()) {

			case timestampDesc:

				criteria.addOrder (
					Order.desc (
						"timestamp"));

				break;

			default:

				throw new IllegalArgumentException ();

			}

		}

		if (search.maxResults () != null) {

			criteria.setMaxResults (
				search.maxResults ());

		}

		criteria.setProjection (
			Projections.id ());

		return findMany (
			Integer.class,
			criteria.list ());

	}

	@Override
	public
	List<ExceptionLogRec> findOldLimit (
			Instant cutoffTime,
			int maxResults) {

		return findMany (
			ExceptionLogRec.class,

			createCriteria (
				ExceptionLogRec.class,
				"_exceptionLog")

			.add (
				Restrictions.lt (
					"_exceptionLog.timestamp",
					cutoffTime))

			.addOrder (
				Order.asc (
					"_exceptionLog.timestamp"))

			.setMaxResults (
				maxResults)

			.list ()

		);

	}

}
