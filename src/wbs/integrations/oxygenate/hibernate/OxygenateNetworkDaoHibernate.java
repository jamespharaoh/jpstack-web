package wbs.integrations.oxygenate.hibernate;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;

import wbs.integrations.oxygenate.model.OxygenateConfigRec;
import wbs.integrations.oxygenate.model.OxygenateNetworkDao;
import wbs.integrations.oxygenate.model.OxygenateNetworkRec;

import wbs.sms.network.model.NetworkRec;

public
class OxygenateNetworkDaoHibernate
	extends HibernateDao
	implements OxygenateNetworkDao {

	@Override
	public
	Optional <OxygenateNetworkRec> findByChannel (
			@NonNull OxygenateConfigRec oxygenateConfig,
			@NonNull String channel) {

		return findOne (
				"findByChannel (oxygenateConfig, channel)",
				OxygenateNetworkRec.class,

			createCriteria (
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

	@Override
	public
	Optional <OxygenateNetworkRec> find (
			@NonNull OxygenateConfigRec oxygenateConfig,
			@NonNull NetworkRec network) {

		return findOne (
			"find (oxygenateConfig, network)",
			OxygenateNetworkRec.class,

			createCriteria  (
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
