package wbs.smsapps.manualresponder.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelperMethods;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

import wbs.utils.random.RandomLogic;

public
class ManualResponderNumberObjectHelperMethodsImplementation
	implements ManualResponderNumberObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	// implementation

	@Override
	public
	ManualResponderNumberRec findOrCreate (
			@NonNull ManualResponderRec manualResponder,
			@NonNull NumberRec number) {

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

				.setCode (
					randomLogic.generateNumericNoZero (
						8))

			);

		}

		return manualResponderNumber;

	}

}
