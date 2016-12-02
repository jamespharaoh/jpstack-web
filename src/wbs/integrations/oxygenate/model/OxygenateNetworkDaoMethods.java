package wbs.integrations.oxygenate.model;

import com.google.common.base.Optional;

import wbs.sms.network.model.NetworkRec;

public
interface OxygenateNetworkDaoMethods {

	Optional <OxygenateNetworkRec> find (
			OxygenateConfigRec oxygen8Config,
			NetworkRec network);

	Optional <OxygenateNetworkRec> findByChannel (
			OxygenateConfigRec oxygen8Config,
			String channel);

}