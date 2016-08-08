package wbs.integrations.mig.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;

import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.integrations.mig.model.MigNetworkObjectHelper;
import wbs.integrations.mig.model.MigNetworkRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.logic.NumberLogic;
import wbs.sms.number.core.model.NumberRec;

public
class MigLogicImplementation
	implements MigLogic {

	// dependencies

	@Inject
	MigNetworkObjectHelper migNetworkHelper;

	@Inject
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

		Optional<MigNetworkRec> currentNetworkOptional =
			migNetworkHelper.find (
				number.getNetwork ().getId ());

		if (

			isPresent (
				currentNetworkOptional)

			&& equal (
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
