package wbs.smsapps.manualresponder.hibernate;

import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;

import lombok.NonNull;
import wbs.framework.hibernate.HibernateDao;
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

	@Override
	public
	List<ManualResponderRequestRec> findRecentLimit (
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number,
			@NonNull Long maxResults) {

		return findMany (
			"findRecentLimit (manualResponder, number, maxResults)",
			ManualResponderRequestRec.class,

			createCriteria (
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

	@Override
	public
	Criteria searchCriteria (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =

			createCriteria (
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

			List<Criterion> filterCriteria =
				new ArrayList<Criterion> ();

			if (
				isNotEmpty (
					search.filterManualResponderIds ())
			) {

				filterCriteria.add (
					Restrictions.in (
						"_manualResponder.id",
						search.filterManualResponderIds ()));

			}

			if (
				isNotEmpty (
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

	@Override
	public
	List<Long> searchIds (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =
			searchCriteria (
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
			"searchIds (search)",
			Long.class,
			criteria);

	}

	@Override
	public
	Criteria searchServiceReportCriteria (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =
			searchCriteria (
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

	@Override
	public
	List<Long> searchServiceReportIds (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =
			searchCriteria (
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

	@Override
	public
	List<ManualResponderServiceReport> searchServiceReports (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =
			searchServiceReportCriteria (
				search);

		return findMany (
			"searchServiceReports (search)",
			ManualResponderServiceReport.class,
			criteria);

	}

	@Override
	public
	List<ManualResponderServiceReport> searchServiceReports (
			@NonNull ManualResponderRequestSearch search,
			@NonNull List<Long> objectIds) {

		Criteria criteria =
			searchServiceReportCriteria (
				search);

		criteria.add (
			Restrictions.in (
				"_manualResponder.id",
				objectIds));

		return findOrdered (
			ManualResponderServiceReport.class,
			objectIds,
			criteria.list ());

	}

	@Override
	public
	Criteria searchOperatorReportCriteria (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =
			searchCriteria (
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

	@Override
	public
	List<Long> searchOperatorReportIds (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =
			searchCriteria (
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

	@Override
	public
	List<ManualResponderOperatorReport> searchOperatorReports (
			@NonNull ManualResponderRequestSearch search) {

		Criteria criteria =
			searchOperatorReportCriteria (
				search);

		return findMany (
			"searchOperatorReports (search)",
			ManualResponderOperatorReport.class,
			criteria);

	}

	@Override
	public
	List<ManualResponderOperatorReport> searchOperatorReports (
			@NonNull ManualResponderRequestSearch search,
			@NonNull List<Long> objectIds) {

		Criteria criteria =
			searchOperatorReportCriteria (
				search);

		criteria.add (
			Restrictions.in (
				"_processedByUser.id",
				objectIds));

		return findOrdered (
			ManualResponderOperatorReport.class,
			objectIds,
			criteria.list ());

	}

}
