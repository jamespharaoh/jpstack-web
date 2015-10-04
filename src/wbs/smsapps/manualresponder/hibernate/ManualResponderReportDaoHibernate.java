package wbs.smsapps.manualresponder.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Interval;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.user.model.UserRec;
import wbs.smsapps.manualresponder.model.ManualResponderReportDao;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

public
class ManualResponderReportDaoHibernate
	extends HibernateDao
	implements ManualResponderReportDao {

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
					instantToDate (
						processedTimeInterval.getStart ().toInstant ())))

			.add (
				Restrictions.lt (
					"_manualResponderReport.processedTime",
					instantToDate (
						processedTimeInterval.getEnd ().toInstant ())))

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
					instantToDate (
						processedTimeInterval.getStart ().toInstant ())))

			.add (
				Restrictions.lt (
					"_manualResponderReport.processedTime",
					instantToDate (
						processedTimeInterval.getEnd ().toInstant ())))

			.list ());

	}

}
