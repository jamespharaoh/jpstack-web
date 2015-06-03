package wbs.integrations.smsarena.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.smsarena.model.SmsArenaInboundLogDao;
import wbs.integrations.smsarena.model.SmsArenaInboundLogRec;
import wbs.integrations.smsarena.model.SmsArenaInboundLogRec.SmsArenaInboundLogSearch;

public class SmsArenaInboundLogDaoHibernate
	extends HibernateDao
	implements SmsArenaInboundLogDao {
	
	// implementation
	
	@Override
	public
	List<Integer> searchIds (
			SmsArenaInboundLogSearch search) {
	
		Criteria criteria =
			createCriteria (
				SmsArenaInboundLogRec.class,
				"_smsArenaInboundLog");
	
		// restrict by route
	
		if (search.getRouteId () != null) {
	
			criteria.add (
				Restrictions.eq (
					"_smsArenaInboundLog.route.id",
					search.getRouteId ()));
	
		}
	
		// restrict by timestamp
	
		if (search.getTimestampAfter () != null) {
	
			criteria.add (
				Restrictions.ge (
					"_smsArenaInboundLog.timestamp",
					search.getTimestampAfter ()));
	
		}
	
		if (search.getTimestampBefore () != null) {
	
			criteria.add (
				Restrictions.lt (
					"_smsArenaInboundLog.timestamp",
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
