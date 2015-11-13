package wbs.smsapps.broadcast.model;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;
import wbs.sms.number.core.model.NumberRec;

public
class BroadcastNumberObjectHelperImplementation
	implements BroadcastNumberObjectHelperMethods {

	@Inject
	Provider<BroadcastNumberObjectHelper> broadcastNumberHelper;

	@Override
	public
	BroadcastNumberRec findOrCreate (
			@NonNull BroadcastRec broadcast,
			@NonNull NumberRec number) {

		// find existing

		BroadcastNumberRec broadcastNumber =
			broadcastNumberHelper.get ().find (
				broadcast,
				number);

		if (broadcastNumber != null)
			return broadcastNumber;

		// create new

		broadcastNumber =
			broadcastNumberHelper.get ().insert (
				broadcastNumberHelper.get ().createInstance ()

			.setBroadcast (
				broadcast)

			.setNumber (
				number)

			.setState (
				BroadcastNumberState.removed)

		);

		// update broadcast

		broadcast

			.setNumRemoved (
				broadcast.getNumRemoved () + 1);

		// return

		return broadcastNumber;

	}

	@Override
	public
	BroadcastNumberRec find (
			BroadcastRec broadcast,
			NumberRec number) {

		return broadcastNumberHelper.get ()
			.findByBroadcastAndNumber (
				broadcast.getId (),
				number.getId ());

	}

	@Override
	public
	List<BroadcastNumberRec> findAcceptedLimit (
			BroadcastRec broadcast,
			int limit) {

		return broadcastNumberHelper.get ()
			.findAcceptedByBroadcastLimit (
				broadcast.getId (),
				limit);

	}

}