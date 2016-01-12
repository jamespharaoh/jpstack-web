package wbs.platform.event.hibernate;

import java.util.List;

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
			EventLinkRec.class,

			createQuery (
				"FROM EventLinkRec el " +
				"WHERE el.typeId = :typeId " +
				"AND el.refId = :refId")

			.setInteger (
				"typeId",
				(int) (long)
				typeId)

			.setInteger (
				"refId",
				(int) (long)
				refId)

			.list ());

	}

}
