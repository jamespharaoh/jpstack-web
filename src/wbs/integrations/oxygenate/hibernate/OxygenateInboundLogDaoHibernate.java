package wbs.integrations.oxygenate.hibernate;

import static wbs.utils.etc.Misc.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.TaskLogger;

import wbs.integrations.oxygenate.model.OxygenateInboundLogDao;
import wbs.integrations.oxygenate.model.OxygenateInboundLogRec;
import wbs.integrations.oxygenate.model.OxygenateInboundLogSearch;

public
class OxygenateInboundLogDaoHibernate
	extends HibernateDao
	implements OxygenateInboundLogDao {

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OxygenateInboundLogSearch search) {

		Criteria criteria =
			createCriteria (
				OxygenateInboundLogRec.class,
				"_oxygenateInboundLog");

		// restrict by route

		if (
			isNotNull (
				search.routeId ())
		) {

			criteria.add (
				Restrictions.eq (
					"_oxygenateInboundLog.route.id",
					search.routeId ()));

		}

		// restrict by timestamp

		if (
			isNotNull (
				search.timestamp ())
		) {

			criteria.add (
				Restrictions.ge (
					"_oxygenateInboundLog.timestamp",
					search.timestamp ().start ()));

			criteria.add (
				Restrictions.lt (
					"_oxygenateInboundLog.timestamp",
					search.timestamp ().end ()));

		}

		// restrict by details

		if (
			isNotNull (
				search.details ())
		) {

			criteria.add (
				Restrictions.ilike (
					"_oxygenateInboundLog.details",
					"%" + search.details () + "%"));

		}

		// restrict by type

		if (
			isNotNull (
				search.type ())
		) {

			criteria.add (
				Restrictions.eq (
					"_oxygenateInboundLog.type",
					search.type ()));

		}

		// restrict by success

		if (
			isNotNull (
				search.success ())
		) {

			criteria.add (
				Restrictions.eq (
					"_oxygenateInboundLog.success",
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
			"searchIds (oxygenateInboundLogSearch)",
			Long.class,
			criteria);

	}

}
