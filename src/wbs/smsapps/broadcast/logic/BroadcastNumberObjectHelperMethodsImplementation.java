package wbs.smsapps.broadcast.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastNumberObjectHelperMethods;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastRec;

public
class BroadcastNumberObjectHelperMethodsImplementation
	implements BroadcastNumberObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	BroadcastNumberObjectHelper broadcastNumberHelper;

	// implementation

	@Override
	public
	BroadcastNumberRec findOrCreate (
			@NonNull BroadcastRec broadcast,
			@NonNull NumberRec number) {

		// find existing

		BroadcastNumberRec broadcastNumber =
			broadcastNumberHelper.find (
				broadcast,
				number);

		if (broadcastNumber != null)
			return broadcastNumber;

		// create new

		broadcastNumber =
			broadcastNumberHelper.insert (
				broadcastNumberHelper.createInstance ()

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

}