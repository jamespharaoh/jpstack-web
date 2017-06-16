package wbs.integrations.oxygenate.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;
import static wbs.utils.string.StringUtils.keyEqualsString;
import static wbs.utils.string.StringUtils.objectToString;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.integrations.oxygenate.model.OxygenateInboundLogDao;
import wbs.integrations.oxygenate.model.OxygenateInboundLogRec;
import wbs.integrations.oxygenate.model.OxygenateInboundLogSearch;

public
class OxygenateInboundLogDaoHibernate
	extends HibernateDao
	implements OxygenateInboundLogDao {

	// singleton depndencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull OxygenateInboundLogSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
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
				transaction,
				Long.class,
				criteria);

		}

	}

	@Override
	public
	List <OxygenateInboundLogRec> findOlderThanLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Instant timestamp,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOlderThanLimit",
					keyEqualsString (
						"timestamp",
						objectToString (
							timestamp)),
					keyEqualsDecimalInteger (
						"maxItems",
						maxResults));

		) {

			return findMany (
				transaction,
				OxygenateInboundLogRec.class,

				createCriteria (
					transaction,
					OxygenateInboundLogRec.class)

				.add (
					Restrictions.lt (
						"timestamp",
						timestamp))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
