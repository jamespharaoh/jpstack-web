package wbs.smsapps.manualresponder.hibernate;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.hibernate.HibernateDao;
import wbs.smsapps.manualresponder.model.ManualResponderReportDao;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

public
class ManualResponderReportDaoHibernate
	extends HibernateDao
	implements ManualResponderReportDao {

	@Override
	public
	List<ManualResponderReportRec> find (
			Interval timestampInterval) {

		return findMany (
			ManualResponderReportRec.class,

			createQuery (
				"FROM ManualResponderReportRec report " +
				"WHERE report.manualResponderRequest.timestamp >= :from " +
					"AND report.manualResponderRequest.timestamp < :to")

			.setTimestamp (
				"from",
				timestampInterval.getStart ().toDate ())

			.setTimestamp (
				"to",
				timestampInterval.getEnd ().toDate ())

			.list ());

	}


}
