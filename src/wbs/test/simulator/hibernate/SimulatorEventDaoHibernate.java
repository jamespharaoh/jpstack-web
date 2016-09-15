package wbs.test.simulator.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import lombok.NonNull;
import wbs.framework.hibernate.HibernateDao;
import wbs.test.simulator.model.SimulatorEventDao;
import wbs.test.simulator.model.SimulatorEventRec;

public
class SimulatorEventDaoHibernate
	extends HibernateDao
	implements SimulatorEventDao {

	@Override
	public
	List <SimulatorEventRec> findAfterLimit (
			@NonNull Long afterId,
			@NonNull Long maxResults) {

		return findMany (
			"findAfterLimit (afterId, maxResults)",
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
				toJavaIntegerRequired (
					maxResults))

		);

	}

}
