package wbs.test.simulator.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.test.simulator.model.SimulatorEventDao;
import wbs.test.simulator.model.SimulatorEventRec;

public
class SimulatorEventDaoHibernate
	extends HibernateDao
	implements SimulatorEventDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <SimulatorEventRec> findAfterLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Long afterId,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAfterLimit");

		) {

			return findMany (
				transaction,
				SimulatorEventRec.class,

				createCriteria (
					transaction,
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

}
