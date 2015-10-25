package wbs.platform.event.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.event.model.EventDao;
import wbs.platform.event.model.EventRec;
import wbs.platform.event.model.EventSearch;

public
class EventDaoHibernate
	extends HibernateDao
	implements EventDao {

	@Override
	public
	List<Integer> searchIds (
			EventSearch eventSearch) {

		Criteria criteria =
			createCriteria (
				EventRec.class,
				"_event");

		if (
			isNotNull (
				eventSearch.timestampAfter ())
		) {

			criteria.add (
				Restrictions.ge (
					"_event.timestamp",
					instantToDate (
						eventSearch.timestampAfter ())));

		}

		if (
			isNotNull (
				eventSearch.timestampBefore ())
		) {

			criteria.add (
				Restrictions.lt (
					"_event.timestamp",
					instantToDate (
						eventSearch.timestampBefore ())));

		}

		// add default order

		criteria

			.addOrder (
				Order.desc (
					"_event.timestamp"));

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
