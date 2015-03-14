package wbs.integrations.oxygen8.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import wbs.framework.hibernate.HibernateDao;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogDao;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogRec;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogRec.Oxygen8InboundLogSearch;

public
class Oxygen8InboundLogDaoHibernate
	extends HibernateDao
	implements Oxygen8InboundLogDao {

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Oxygen8InboundLogSearch oxygen8InboundLogSearch) {

		Criteria criteria =
			createCriteria (
				Oxygen8InboundLogRec.class);

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
