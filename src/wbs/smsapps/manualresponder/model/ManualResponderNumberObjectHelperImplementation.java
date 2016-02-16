package wbs.smsapps.manualresponder.model;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.sms.number.core.model.NumberRec;

public
class ManualResponderNumberObjectHelperImplementation
	implements ManualResponderNumberObjectHelperMethods {

	// dependencies

	@Inject
	Provider<ManualResponderNumberObjectHelper>
	manualResponderNumberHelperProvider;

	// implementation

	@Override
	public
	ManualResponderNumberRec findOrCreate (
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number) {

		ManualResponderNumberObjectHelper manualResponderNumberHelper =
			manualResponderNumberHelperProvider.get ();

		// find or create number

		ManualResponderNumberRec manualResponderNumber =
			manualResponderNumberHelper.find (
				manualResponder,
				number);

		if (manualResponderNumber == null) {

			manualResponderNumber =
				manualResponderNumberHelper.insert (
					manualResponderNumberHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setNumber (
					number)

			);

		}

		return manualResponderNumber;

	}

}
