package wbs.test.simulator.hibernate;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.test.simulator.model.SimulatorEventDao;
import wbs.test.simulator.model.SimulatorEventRec;

public
class SimulatorEventDaoHibernate
	extends HibernateDao
	implements SimulatorEventDao {

	@Override
	public
	List<SimulatorEventRec> findAfterLimit (
			int afterId,
			int maxResults) {

		return findMany (
			SimulatorEventRec.class,

			createCriteria (
				SimulatorEventRec.class,
				"_simulatorEvent")

			.add (
				Restrictions.gt (
					"_simulatorEvent.id",
					afterId))

			.addOrder (
				Order.asc (
					"_simulatorEvent.id"))

			.setMaxResults (
				maxResults)

			.list ());

	}

}
