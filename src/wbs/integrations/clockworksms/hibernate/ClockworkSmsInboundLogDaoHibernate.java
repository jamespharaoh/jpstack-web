package wbs.integrations.clockworksms.hibernate;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogDao;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogRec;
import wbs.integrations.clockworksms.model.ClockworkSmsInboundLogSearch;

public 
class ClockworkSmsInboundLogDaoHibernate
	extends HibernateDao
	implements ClockworkSmsInboundLogDao {

	// implementation

	@Override
	public
	List<Integer> searchIds (
			@NonNull ClockworkSmsInboundLogSearch search) {

		Criteria criteria =
			createCriteria (
				ClockworkSmsInboundLogRec.class,
				"_clockworkSmsInboundLog");

		// restrict by route

		if (
			isNotNull (
				search.routeId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_clockworkSmsInboundLog.route.id",
					search.routeId ()));

		}

		// restrict by timestamp

		if (
			isNotNull (
				search.timestamp ())
		) {

			criteria.add (
				Restrictions.ge (
					"_clockworkSmsInboundLog.timestamp",
					search.timestamp ().start ()));

			criteria.add (
				Restrictions.lt (
					"_clockworkSmsInboundLog.timestamp",
					search.timestamp ().end ()));

		}

		// restrict by details

		if (
			isNotNull (
				search.details ())
		) {

			criteria.add (
				Restrictions.ilike (
					"clockworkSmsInboundLog.details",
					search.details ()));

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
			"searchIds (search)",
			Integer.class,
			criteria);

	}

}
