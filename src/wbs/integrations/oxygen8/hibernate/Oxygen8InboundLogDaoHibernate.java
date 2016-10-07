package wbs.integrations.oxygen8.hibernate;

import static wbs.utils.etc.Misc.isNotNull;

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
	List <Long> searchIds (
			@NonNull Oxygen8InboundLogSearch search) {

		Criteria criteria =
			createCriteria (
				Oxygen8InboundLogRec.class,
				"_oxygen8InboundLog");

		// restrict by route

		if (
			isNotNull (
				search.routeId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_oxygen8InboundLog.route.id",
					search.routeId ()));

		}

		// restrict by timestamp

		if (
			isNotNull (
				search.timestamp ())
		) {

			criteria.add (
				Restrictions.ge (
					"_oxygen8InboundLog.timestamp",
					search.timestamp ().start ()));

			criteria.add (
				Restrictions.lt (
					"_oxygen8InboundLog.timestamp",
					search.timestamp ().end ()));

		}

		// restrict by details

		if (
			isNotNull (
				search.details ())
		) {

			criteria.add (
				Restrictions.ilike (
					"_oxygen8InboundLog.details",
					"%" + search.details () + "%"));

		}

		// restrict by type

		if (
			isNotNull (
				search.type ())
		) {

			criteria.add (
				Restrictions.eq (
					"_oxygen8InboundLog.type",
					search.type ()));

		}

		// restrict by success

		if (
			isNotNull (
				search.success ())
		) {

			criteria.add (
				Restrictions.eq (
					"_oxygen8InboundLog.success",
					search.success ()));

		}

		// add default order

		criteria

			.addOrder (
				Order.desc (
					"id"));

		// set to return ids only

		criteria

			.setProjection (
				Projections.id ());

		// perform and return

		return findMany (
			"searchIds (oxygen8InboundLogSearch)",
			Long.class,
			criteria);

	}

}
