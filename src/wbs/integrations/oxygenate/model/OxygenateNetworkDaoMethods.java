package wbs.integrations.oxygenate.model;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.sms.network.model.NetworkRec;

public
interface OxygenateNetworkDaoMethods {

	Optional <OxygenateNetworkRec> find (
			Transaction parentTransaction,
			OxygenateConfigRec oxygen8Config,
			NetworkRec network);

	Optional <OxygenateNetworkRec> findByChannel (
			Transaction parentTransaction,
			OxygenateConfigRec oxygen8Config,
			String channel);

}