package wbs.platform.event.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.event.model.EventLinkDao;
import wbs.platform.event.model.EventLinkRec;

public
class EventLinkDaoHibernate
	extends HibernateDao
	implements EventLinkDao {

	@Override
	public
	List<EventLinkRec> findByTypeAndRef (
			long typeId,
			long refId) {

		return findMany (
			"findByTypeAndRef (typeId, refId)",
			EventLinkRec.class,

			createCriteria (
				EventLinkRec.class,
				"_eventLink")

			.add (
				Restrictions.eq (
					"_eventLink.typeId",
					typeId))

			.add (
				Restrictions.eq (
					"_eventLink.refId",
					refId))

		);

	}

}
