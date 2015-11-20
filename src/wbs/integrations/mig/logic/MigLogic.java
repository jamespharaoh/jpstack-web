package wbs.integrations.mig.logic;

import wbs.sms.network.model.NetworkRec;

public
interface MigLogic {

	NetworkRec getNetwork (
		String connection,
		String destAddress);

}