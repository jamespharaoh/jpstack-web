package wbs.integrations.oxygenate.hibernate;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.integrations.oxygenate.model.OxygenateConfigRec;
import wbs.integrations.oxygenate.model.OxygenateNetworkDao;
import wbs.integrations.oxygenate.model.OxygenateNetworkRec;

import wbs.sms.network.model.NetworkRec;

public
class OxygenateNetworkDaoHibernate
	extends HibernateDao
	implements OxygenateNetworkDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <OxygenateNetworkRec> findByChannel (
			@NonNull Transaction parentTransaction,
			@NonNull OxygenateConfigRec oxygenateConfig,
			@NonNull String channel) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByChannel");

		) {

			return findOne (
					transaction,
					OxygenateNetworkRec.class,

				createCriteria (
					transaction,
					OxygenateNetworkRec.class,
					"_oxygenateNetwork")

				.add (
					Restrictions.eq (
						"_oxygenateNetwork.oxygenateConfig",
						oxygenateConfig))

				.add (
					Restrictions.eq (
						"_oxygenateNetwork.channel",
						channel))

			);

		}

	}

	@Override
	public
	Optional <OxygenateNetworkRec> find (
			@NonNull Transaction parentTransaction,
			@NonNull OxygenateConfigRec oxygenateConfig,
			@NonNull NetworkRec network) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOne (
				transaction,
				OxygenateNetworkRec.class,

				createCriteria  (
					transaction,
					OxygenateNetworkRec.class,
					"_oxygenateNetwork")

				.add (
					Restrictions.eq (
						"_oxygenateNetwork.oxygenateConfig",
						oxygenateConfig))

				.add (
					Restrictions.eq (
						"_oxygenateNetwork.network",
						network))

			);

		}

	}

}
