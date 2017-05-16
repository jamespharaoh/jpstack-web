package wbs.smsapps.manualresponder.hibernate;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.manualresponder.model.ManualResponderOperatorReport;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestDao;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;
import wbs.smsapps.manualresponder.model.ManualResponderServiceReport;

public
class ManualResponderRequestDaoHibernate
	extends HibernateDao
	implements ManualResponderRequestDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <ManualResponderRequestRec> findRecentLimit (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRecentLimit");

		) {

			return findMany (
				transaction,
				ManualResponderRequestRec.class,

				createCriteria (
					transaction,
					ManualResponderRequestRec.class,
					"_manualResponderRequest")

				.createAlias (
					"_manualResponderRequest.manualResponderNumber",
					"_manualResponderNumber")

				.add (
					Restrictions.eq (
						"_manualResponderNumber.manualResponder",
						manualResponder))

				.add (
					Restrictions.eq (
						"_manualResponderRequest.number",
						number))

				.addOrder (
					Order.desc (
						"_manualResponderRequest.timestamp"))

				.addOrder (
					Order.desc (
						"_manualResponderRequest.id"))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

	@Override
	public
	Criteria searchCriteria (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchCriteria");

		) {

			Criteria criteria =

				createCriteria (
					transaction,
					ManualResponderRequestRec.class,
					"_manualResponderRequest")

				.createAlias (
					"_manualResponderRequest.manualResponderNumber",
					"_manualResponderNumber")

				.createAlias (
					"_manualResponderNumber.manualResponder",
					"_manualResponder")

				.createAlias (
					"_manualResponder.slice",
					"_manualResponderSlice")

				.createAlias (
					"_manualResponderRequest.number",
					"_number")

				.createAlias (
					"_manualResponderRequest.user",
					"_processedByUser",
					JoinType.LEFT_OUTER_JOIN)

				.createAlias (
					"_processedByUser.slice",
					"_processedByUserSlice",
					JoinType.LEFT_OUTER_JOIN);

			if (search.manualResponderId () != null) {

				criteria.add (
					Restrictions.eq (
						"_manualResponder.id",
						search.manualResponderId ()));

			}

			if (
				isNotNull (
					search.manualResponderSliceId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_manualResponderSlice.id",
						search.manualResponderSliceId ()));

			}

			if (search.numberLike () != null) {

				criteria.add (
					Restrictions.like (
						"_number.number",
						search.numberLike ()));

			}

			if (
				isNotNull (
					search.processedByUserId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_manualResponderRequest.user.id",
						search.processedByUserId ()));

			}

			if (
				isNotNull (
					search.processedByUserSliceId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_processedByUserSlice.id",
						search.processedByUserSliceId ()));

			}

			if (
				isNotNull (
					search.createdTime ())
			) {

				criteria.add (
					Restrictions.ge (
						"_manualResponderRequest.timestamp",
						search.createdTime ().start ()));

				criteria.add (
					Restrictions.lt (
						"_manualResponderRequest.timestamp",
						search.createdTime ().end ()));

			}

			if (
				isNotNull (
					search.processedTime ())
			) {

				criteria.add (
					Restrictions.ge (
						"_manualResponderRequest.processedTime",
						search.processedTime ().start ()));

				criteria.add (
					Restrictions.lt (
						"_manualResponderRequest.processedTime",
						search.processedTime ().end ()));

			}

			// apply filter

			if (search.filter ()) {

				List <Criterion> filterCriteria =
					new ArrayList<> ();

				if (
					collectionIsNotEmpty (
						search.filterManualResponderIds ())
				) {

					filterCriteria.add (
						Restrictions.in (
							"_manualResponder.id",
							search.filterManualResponderIds ()));

				}

				if (
					collectionIsNotEmpty (
						search.filterProcessedByUserIds ())
				) {

					filterCriteria.add (
						Restrictions.in (
							"_processedByUser.id",
							search.filterProcessedByUserIds ()));

				}

				criteria.add (
					Restrictions.or (
						filterCriteria.toArray (
							new Criterion [] {})));

			}

			// return

			return criteria;

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				searchCriteria (
					transaction,
					search);

			// set order

			switch (search.order ()) {

			case timestampDesc:

				criteria

					.addOrder (
						Order.desc (
							"timestamp"))

					.addOrder (
						Order.desc (
							"id"));

				break;

			default:

				throw new RuntimeException ();

			}

			// set projection

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

	@Override
	public
	Criteria searchServiceReportCriteria (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchServiceReportCriteria");

		) {

			Criteria criteria =
				searchCriteria (
					transaction,
					search);

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.property (
						"_manualResponderNumber.manualResponder"),
					"manualResponder")

				.add (
					Projections.sum (
						"_manualResponderRequest.numFreeMessages"),
					"numFree")

				.add (
					Projections.sum (
						"_manualResponderRequest.numBilledMessages"),
					"numBilled")

				.add (
					Projections.groupProperty (
						"_manualResponderNumber.manualResponder"))

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					ManualResponderServiceReport.class));

			return criteria;

		}

	}

	@Override
	public
	List <Long> searchServiceReportIds (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchServiceReportIds");

		) {

			Criteria criteria =
				searchCriteria (
					transaction,
					search);

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.distinct (
						Projections.property (
							"_manualResponder.id")))

				.add (
					Projections.groupProperty (
						"_manualResponder.id"))

				.add (
					Projections.groupProperty (
						"_manualResponder.code"))

				.add (
					Projections.groupProperty (
						"_manualResponderSlice.code"))

			);

			criteria.addOrder (
				Order.asc (
					"_manualResponderSlice.code"));

			criteria.addOrder (
				Order.asc (
					"_manualResponder.code"));

			return findIdsOnly (
				criteria.list ());

		}

	}

	@Override
	public
	List <ManualResponderServiceReport> searchServiceReports (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchServiceReports");

		) {

			Criteria criteria =
				searchServiceReportCriteria (
					transaction,
					search);

			return findMany (
				transaction,
				ManualResponderServiceReport.class,
				criteria);

		}

	}

	@Override
	public
	List <Optional <ManualResponderServiceReport>> searchServiceReports (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search,
			@NonNull List <Long> objectIds) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchServiceReports");

		) {

			Criteria criteria =
				searchServiceReportCriteria (
					transaction,
					search);

			criteria.add (
				Restrictions.in (
					"_manualResponder.id",
					objectIds));

			return findOrdered (
				transaction,
				ManualResponderServiceReport.class,
				objectIds,
				criteria.list ());

		}

	}

	@Override
	public
	Criteria searchOperatorReportCriteria (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchOperatorReportCriteria");

		) {

			Criteria criteria =
				searchCriteria (
					transaction,
					search);

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.property (
						"_manualResponderRequest.user"),
					"user")

				.add (
					Projections.sum (
						"_manualResponderRequest.numFreeMessages"),
					"numFree")

				.add (
					Projections.sum (
						"_manualResponderRequest.numBilledMessages"),
					"numBilled")

				.add (
					Projections.groupProperty (
						"_manualResponderRequest.user"))

			);

			criteria.setResultTransformer (
				Transformers.aliasToBean (
					ManualResponderOperatorReport.class));

			return criteria;

		}

	}

	@Override
	public
	List <Long> searchOperatorReportIds (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchOperatorReportIds");

		) {

			Criteria criteria =
				searchCriteria (
					transaction,
					search);

			criteria.setProjection (
				Projections.projectionList ()

				.add (
					Projections.distinct (
						Projections.property (
							"_manualResponderRequest.user.id")))

				.add (
					Projections.groupProperty (
						"_manualResponderRequest.user.id"))

				.add (
					Projections.groupProperty (
						"_processedByUserSlice.code"))

				.add (
					Projections.groupProperty (
						"_processedByUser.username"))

			);

			criteria.addOrder (
				Order.asc (
					"_processedByUserSlice.code"));

			criteria.addOrder (
				Order.asc (
					"_processedByUser.username"));

			return findIdsOnly (
				criteria.list ());

		}

	}

	@Override
	public
	List <ManualResponderOperatorReport> searchOperatorReports (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchOperatorReports");

		) {

			Criteria criteria =
				searchOperatorReportCriteria (
					transaction,
					search);

			return findMany (
				transaction,
				ManualResponderOperatorReport.class,
				criteria);

		}

	}

	@Override
	public
	List <Optional <ManualResponderOperatorReport>> searchOperatorReports (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRequestSearch search,
			@NonNull List <Long> objectIds) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchOperatorReports");

		) {

			Criteria criteria =
				searchOperatorReportCriteria (
					transaction,
					search);

			criteria.add (
				Restrictions.in (
					"_processedByUser.id",
					objectIds));

			return findOrdered (
				transaction,
				ManualResponderOperatorReport.class,
				objectIds,
				criteria.list ());

		}

	}

}
