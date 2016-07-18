package wbs.integrations.oxygen8.hibernate;

import java.util.List;

import lombok.NonNull;

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
			@NonNull Oxygen8InboundLogSearch oxygen8InboundLogSearch) {

		Criteria criteria =
			createCriteria (
				Oxygen8InboundLogRec.class,
				"_oxygen8InboundLog");

		// restrict by route

		if (oxygen8InboundLogSearch.getRouteId () != null) {

			criteria.add (
				Restrictions.eq (
					"_oxygen8InboundLog.route.id",
					oxygen8InboundLogSearch.getRouteId ()));

		}

		// restrict by timestamp

		if (oxygen8InboundLogSearch.getTimestampAfter () != null) {

			criteria.add (
				Restrictions.ge (
					"_oxygen8InboundLog.timestamp",
					oxygen8InboundLogSearch.getTimestampAfter ()));

		}

		if (oxygen8InboundLogSearch.getTimestampBefore () != null) {

			criteria.add (
				Restrictions.lt (
					"_oxygen8InboundLog.timestamp",
					oxygen8InboundLogSearch.getTimestampBefore ()));

		}

		// restrict by details

		if (oxygen8InboundLogSearch.getDetails () != null) {

			criteria.add (
				Restrictions.ilike (
					"_oxygen8InboundLog.details",
					oxygen8InboundLogSearch.getDetails ()));

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
			"searchIds (oxygen8InboundLogSearch)",
			Integer.class,
			criteria);

	}

}
