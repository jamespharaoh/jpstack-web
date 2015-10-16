package wbs.integrations.oxygen8.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogDao;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogRec;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogSearch;

public
class Oxygen8InboundLogDaoHibernate
	extends HibernateDao
	implements Oxygen8InboundLogDao {

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Oxygen8InboundLogSearch search) {

		Criteria criteria =
			createCriteria (
				Oxygen8InboundLogRec.class,
				"_oxygen8InboundLog");

		// restrict by route

		if (search.getRouteId () != null) {

			criteria.add (
				Restrictions.eq (
					"_oxygen8InboundLog.route.id",
					search.getRouteId ()));

		}

		// restrict by timestamp

		if (search.getTimestampAfter () != null) {

			criteria.add (
				Restrictions.ge (
					"_oxygen8InboundLog.timestamp",
					search.getTimestampAfter ()));

		}

		if (search.getTimestampBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"_oxygen8InboundLog.timestamp",
					search.getTimestampBefore ()));

		}

		// restrict by details

		if (search.getDetails () != null) {

			criteria.add (
				Restrictions.ilike (
					"_oxygen8InboundLog.details",
					search.getDetails ()));

		}

		// add default order

		criteria

			.addOrder (
				Order.desc ("id"));

		// set to return ids only

		criteria

			.setProjection (
				Projections.id ());

		// perform and return

		return findMany (
			Integer.class,
			criteria.list ());

	}

}
