package wbs.test.simulator.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.test.simulator.model.SimEventDao;
import wbs.test.simulator.model.SimEventRec;

public
class SimEventDaoHibernate
	extends HibernateDao
	implements SimEventDao {

	@Override
	public
	List<SimEventRec> findAfterLimit (
			int afterId,
			int maxResults) {

		return findMany (
			SimEventRec.class,

			createQuery (
				"FROM SimEventRec simEvent " +
				"WHERE simEvent.id > :afterId " +
				"ORDER BY simEvent.id")

			.setInteger (
				"afterId",
				afterId)

			.setMaxResults (
				maxResults)

			.list ());

	}

}
