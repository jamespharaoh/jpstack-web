package wbs.integrations.oxygen8.model;

import wbs.sms.network.model.NetworkRec;

public
interface Oxygen8NetworkDaoMethods {

	Oxygen8NetworkRec find (
			Oxygen8ConfigRec oxygen8Config,
			NetworkRec network);

	Oxygen8NetworkRec findByChannel (
			Oxygen8ConfigRec oxygen8Config,
			String channel);

}