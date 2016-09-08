package wbs.integrations.mig.logic;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.integrations.mig.model.MigNetworkObjectHelper;
import wbs.integrations.mig.model.MigNetworkRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;

public
class MigLogicImplementation
	implements MigLogic {

	// singleton dependencies

	@SingletonDependency
	MigNetworkObjectHelper migNetworkHelper;

	@SingletonDependency
	NumberLogic numberLogic;

	// implementation

	@Override
	public
	NetworkRec getNetwork (
			@NonNull String connection,
			@NonNull String destAddress) {

		String suffix =
			connection.substring (5);

		MigNetworkRec connectionNetwork =
			migNetworkHelper.findBySuffix (
				suffix);

		if (connectionNetwork == null) {

			throw new RuntimeException (
				stringFormat (
					"Mig connection string invalid: %s",
					connection));

		}

		NumberRec number =
			numberLogic.objectToNumber (
				destAddress);

		Optional <MigNetworkRec> currentNetworkOptional =
			migNetworkHelper.find (
				number.getNetwork ().getId ());

		if (

			optionalIsPresent (
				currentNetworkOptional)

			&& stringEqualSafe (
				connectionNetwork.getSuffix (),
				currentNetworkOptional.get ().getSuffix ())

		) {

			// looks like a virtual network, leave alone

			return currentNetworkOptional.get ().getNetwork ();

		} else {

			// return new network id

			return connectionNetwork.getNetwork ();

		}

	}

}
