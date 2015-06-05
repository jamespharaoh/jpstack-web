package wbs.integrations.smsarena.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.smsarena.model.SmsArenaDlrReportLogDao;
import wbs.integrations.smsarena.model.SmsArenaDlrReportLogRec.SmsArenaDlrReportLogSearch;
import wbs.integrations.smsarena.model.SmsArenaDlrReportLogRec;

public class SmsArenaDlrReportLogDaoHibernate
	extends HibernateDao
	implements SmsArenaDlrReportLogDao {
	
	// implementation
	
	@Override
	public
	List<Integer> searchIds (
			SmsArenaDlrReportLogSearch search) {
	
		Criteria criteria =
			createCriteria (
				SmsArenaDlrReportLogRec.class,
				"_smsArenaDlrReportLog");
	
		// restrict by route
	
		if (search.getRouteId () != null) {
	
			criteria.add (
				Restrictions.eq (
					"_smsArenaDlrReportLog.route.id",
					search.getRouteId ()));
	
		}
	
		// restrict by timestamp
	
		if (search.getTimestampAfter () != null) {
	
			criteria.add (
				Restrictions.ge (
					"_smsArenaDlrReportLog.timestamp",
					search.getTimestampAfter ()));
	
		}
	
		if (search.getTimestampBefore () != null) {
	
			criteria.add (
				Restrictions.lt (
					"_smsArenaDlrReportLog.timestamp",
					search.getTimestampBefore ()));
	
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
			Integer.class,
			criteria.list ());
	
	}

}
