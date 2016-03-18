package wbs.smsapps.manualresponder.hibernate;

public
class ManualResponderReportDaoHibernate {

	/*
	@Override
	public
	List<ManualResponderReportRec> findByProcessedTime (
			Interval processedTimeInterval) {

		return findMany (
			ManualResponderReportRec.class,

			createCriteria (
				ManualResponderReportRec.class,
				"_manualResponderReport")

			.add (
				Restrictions.ge (
					"_manualResponderReport.processedTime",
					processedTimeInterval.getStart ().toInstant ()))

			.add (
				Restrictions.lt (
					"_manualResponderReport.processedTime",
					processedTimeInterval.getEnd ().toInstant ()))

			.list ());

	}

	@Override
	public
	List<ManualResponderReportRec> findByProcessedTime (
			UserRec processedByUser,
			Interval processedTimeInterval) {

		return findMany (
			ManualResponderReportRec.class,

			createCriteria (
				ManualResponderReportRec.class,
				"_manualResponderReport")

			.add (
				Restrictions.eq (
					"_manualResponderReport.processedByUser",
					processedByUser))

			.add (
				Restrictions.ge (
					"_manualResponderReport.processedTime",
					processedTimeInterval.getStart ().toInstant ()))

			.add (
				Restrictions.lt (
					"_manualResponderReport.processedTime",
					processedTimeInterval.getEnd ().toInstant ()))

			.list ());

	}

	@Override
	public
	Long countByProcessedTime (
			UserRec processedByUser,
			Interval processedTimeInterval) {

		Long value =
			findOne (
				Long.class,

			createCriteria (
				ManualResponderReportRec.class,
				"_manualResponderReport")

			.add (
				Restrictions.eq (
					"_manualResponderReport.processedByUser",
					processedByUser))

			.add (
				Restrictions.ge (
					"_manualResponderReport.processedTime",
					processedTimeInterval.getStart ().toInstant ()))

			.add (
				Restrictions.lt (
					"_manualResponderReport.processedTime",
					processedTimeInterval.getEnd ().toInstant ()))

			.setProjection (
				Projections.sum (
					"num"))

			.list ());

		return ifNull (
			value,
			0l);

	}
	*/

}
