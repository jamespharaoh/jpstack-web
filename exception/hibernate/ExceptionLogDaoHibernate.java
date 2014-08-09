package wbs.platform.exception.hibernate;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.exception.model.ExceptionLogDao;
import wbs.platform.exception.model.ExceptionLogRec;

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
	List<ExceptionLogRec> search (
			Map<String,Object> params) {

		Criteria criteria =
			createCriteria (ExceptionLogRec.class);

		for (Map.Entry<String,Object> entry
				: params.entrySet ()) {

			String key =
				entry.getKey ();

			Object value =
				entry.getValue ();

			if (key.equals ("alert")) {

				criteria.add (
					Restrictions.eq (
						"alert",
						value));

			} else if (equal (
					key,
					"limit")) {

				criteria.setMaxResults (
					(Integer)
					value);

			} else if (key.equals ("orderBy")) {

				if (equal (
						value,
						"timestampDesc")) {

					criteria.addOrder (
						Order.desc ("timestamp"));

				} else {

					throw new IllegalArgumentException (
						"Cannot order by " + value);

				}

			} else {

				throw new IllegalArgumentException (
					"Unknown search parameter: " + key);

			}

		}

		return findMany (
			ExceptionLogRec.class,
			criteria.list ());

	}

}
