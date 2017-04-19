package wbs.integrations.clockworksms.hibernate;

import static wbs.utils.etc.Misc.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.TaskLogger;

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
	List <Long> searchIds (
			@NonNull TaskLogger parentTaskLogger,
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
					"_clockworkSmsInboundLog.details",
					"%" + search.details () + "%"));

		}

		// restrict by type

		if (
			isNotNull (
				search.type ())
		) {

			criteria.add (
				Restrictions.eq (
					"_clockworkSmsInboundLog.type",
					search.type ()));

		}

		// restrict by success

		if (
			isNotNull (
				search.success ())
		) {

			criteria.add (
				Restrictions.eq (
					"_clockworkSmsInboundLog.success",
					search.success ()));

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
			Long.class,
			criteria);

	}

}
